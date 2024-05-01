package com.mobile.narciso

//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.FaceDetection

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.TotalCaptureResult
import android.icu.text.SimpleDateFormat
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.mobile.narciso.databinding.FragmentCameraBinding
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {
    //Binding to layout objects
    private var binding: FragmentCameraBinding? = null

    private lateinit var currentPhotoPath: String
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

        //Retrieve username of the logged user
        val prefs =
            requireContext().getSharedPreferences("myemotiontrackerapp", Context.MODE_PRIVATE)
        username = prefs.getString("username", "")!!

        //ask for permissions
        val cameraLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted[Manifest.permission.CAMERA]!!) {
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

    //applico l'inflate delle funzioni sui bottoni
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.takepic?.setOnClickListener {
            // Cattura l'immagine attuale dalla fotocamera
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Ottieni un file di output per salvare l'immagine
            val imageFile = createImageFile()

            // Configura le opzioni di output per l'immagine
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

            try {
                // Cattura l'immagine e salvala nel file specificato
                imageCapture.takePicture(outputFileOptions, cameraExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Photo saved!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onError(imageCaptureError: ImageCaptureException) {
                            // Errore durante il salvataggio dell'immagine
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Photo capture failed: ${imageCaptureError.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            imageCaptureError.printStackTrace()
                        }
                    })
            } catch (e: Exception) {
                // Eccezione generica
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                e.printStackTrace()
                Log.e(TAG, "Error: ${e.message}", e)
            }
        }

        binding?.flashtoggle?.setOnClickListener {
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    "using flash.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        if (!::bitmapBuffer.isInitialized) {
                            bitmapBuffer = Bitmap.createBitmap(
                                image.width,
                                image.height,
                                Bitmap.Config.ARGB_8888
                            )
                        }

                        bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)
                        val imageRotation = image.imageInfo.rotationDegrees

                        // Create and save the image file
                        val imageFile = createImageFile()
                        saveBitmapToFile(bitmapBuffer, imageFile)

                        // You can do further processing or use the saved file here
                        // detectFaces(bitmapBuffer, imageRotation)
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

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
            //activity?.runOnUiThread { Toast.makeText(requireContext(), absolutePath, Toast.LENGTH_SHORT).show()}
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, file: File) {
        val outputStream = FileOutputStream(file)
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream.close()
        }
    }

}