package com.mobile.narciso

import com.google.android.gms.location.R
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.FaceDetection
import com.mobile.narciso.databinding.FragmentCameraBinding
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt


class CameraFragment : Fragment() {
    //Binding to layout objects
    private var binding: FragmentCameraBinding? = null
    private val fragmentCameraBinding
        get() = binding!!

    //Thread that handles camera activity
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var username: String
    private var happinessAccumulator: Double = 0.0
    private var counter: Int = 0
    private var windowArray: ArrayList<Float> = ArrayList<Float>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequesterStarted = false
    private lateinit var locCallback: LocationCallback
    private var firstCall = true

    companion object {
        private const val TAG = "CameraFragment"
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 123
        private const val windowSize = 30
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding?.takepic?.setOnClickListener{

        }

        //Retrieve username of the logged user
        val prefs =
            requireContext().getSharedPreferences("myemotiontrackerapp", Context.MODE_PRIVATE)
        username = prefs.getString("username", "")!!

        //ask for permissions
        val cameraLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted[Manifest.permission.CAMERA]!! && isGranted[Manifest.permission.ACCESS_FINE_LOCATION]!!) {
                //launchLocationRequester()
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        cameraLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Wanted behaviour is that keyboard popup will overlap the fragment
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        //bind layout to Kotlin objects
        binding = FragmentCameraBinding.inflate(inflater)
        return fragmentCameraBinding.root
    }

    override fun onStart() {
        super.onStart()
        // create background thread that will execute image processing
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
        //launchLocationRequester()
    }

    override fun onStop() {
        super.onStop()
        // Shut down background thread
        cameraExecutor.shutdown()
        //stopLocationRequester()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Manage rotation of the screen
            val windowManager = requireActivity().windowManager
            val rotation = windowManager.defaultDisplay.rotation

            // Preview to show on screen
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .build()
                .also {
                    it.setSurfaceProvider(fragmentCameraBinding.viewFinder.surfaceProvider)
                }

            // Workflow to apply for each frame detected
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Discard frames until the processing of the previous one is not completed
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            // The image rotation and RGB image buffer are initialized only once
                            // the analyzer has started running
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        // Copy out RGB bits to the shared bitmap buffer
                        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }

                        val imageRotation = image.imageInfo.rotationDegrees

                        // bitmap buffer contains the captured image
                        //detectFaces(bitmapBuffer, imageRotation)
                    }
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

}