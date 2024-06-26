package com.mobile.narciso

//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import com.google.mlkit.vision.common.InputImage
//import com.google.mlkit.vision.face.FaceDetection

import android.Manifest
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark
import com.mobile.narciso.databinding.FragmentCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraFragment is a Fragment used for capturing and processing images from the device's camera.
 * This fragment uses the CameraX library to handle camera operations and the ML Kit library to detect faces in the captured images.
 *
 * The startCamera method initializes the camera and sets up the image analysis pipeline.
 * The image analysis uses an ImageAnalyzer to process each frame and detect faces using the detectFaces method.
 *
 * The detectFaces method uses the ML Kit's Face Detection API to detect faces in the image.
 * For each detected face, it extracts facial landmarks (like the position of the eyes, nose, mouth, etc.) and stores them in a FaceLandmarks object.
 *
 * The drawLandmarks method is used to draw the detected facial landmarks on the image for visualization.
 *
 * The getFaceLandmarks method returns the last detected facial landmarks. This can be used to access the facial landmarks outside of this fragment.
 *
 * This fragment also handles permissions for using the camera.
 */

//class for taking data from Google API ML kit
data class FaceLandmarks(
    var leftEye: FaceLandmark? = null,
    var rightEye: FaceLandmark? = null,
    var noseBase: FaceLandmark? = null,
    var leftEar: FaceLandmark? = null,
    var rightEar: FaceLandmark? = null,
    var mouthLeft: FaceLandmark? = null,
    var mouthRight: FaceLandmark? = null,
    var mouthBottom: FaceLandmark? = null,
    var leftCheek: FaceLandmark? = null,
    var rightCheek: FaceLandmark? = null,
)

// class for saving only positional information from FaceLandmarks
data class FaceLandmarkClean(
    var leftEye: PointF? = PointF(0f, 0f),
    var rightEye: PointF? = PointF(0f, 0f),
    var noseBase: PointF? = PointF(0f, 0f),
    var leftEar: PointF? = PointF(0f, 0f),
    var rightEar: PointF? = PointF(0f, 0f),
    var mouthLeft: PointF? = PointF(0f, 0f),
    var mouthRight: PointF? = PointF(0f, 0f),
    var mouthBottom: PointF? = PointF(0f, 0f),
    var leftCheek: PointF? = PointF(0f, 0f),
    var rightCheek: PointF? = PointF(0f, 0f),
)

class Faces {
    var faceLandmarks: List<FaceLandmarks>? = null
}

class CameraFragment : Fragment() {
    //Binding to layout objects
    private var binding: FragmentCameraBinding? = null
    private val fragmentCameraBinding
        get() = binding

    //Thread that handles camera activity
    private lateinit var cameraExecutor: ExecutorService
    lateinit var bitmapBuffer: Bitmap
    private lateinit var username: String

    // setting options for landmarks collection
    val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setMinFaceSize(0.15f)
        .enableTracking()
        .build()
    private val faceDetector = FaceDetection.getClient(options)
    var imageRotation: Int = 0
    val faceFinded = Faces()
    val landmarkTSaved = FaceLandmarkClean()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //ask for permissions
        val cameraLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted ->
            if (isGranted[Manifest.permission.CAMERA]!!) {
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
        //requesting camera permission
        cameraLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): ConstraintLayout? {
        //bind layout to Kotlin objects
        binding = FragmentCameraBinding.inflate(inflater)
        return fragmentCameraBinding?.root
    }

    //onViewCreated is called after onCreateView and is used to initialize the camera and start capturing images
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //creating cameraExcutor thread
    override fun onStart() {
        super.onStart()
        // create background thread that will execute image processing
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
    }

    // shutdown everything when the fragment is stopped
    override fun onStop() {
        super.onStop()
        // Shut down background thread
        cameraExecutor.shutdown()
        //stopLocationRequester()
        faceDetector.close()
    }

    //destroying the binding object
    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
        cameraExecutor.shutdown()
        faceDetector.close()
    }

    //rotate the bitmap in order to have the right orientation
    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        // Rotate the source bitmap of angle degrees
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
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
                    it.setSurfaceProvider(fragmentCameraBinding?.viewFinder?.surfaceProvider)
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

                        imageRotation = image.imageInfo.rotationDegrees

                        // bitmap buffer contains the captured image
                        detectFaces(bitmapBuffer, imageRotation)
                    }
                }

            // Select front camera as a default
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))

    }
    fun detectFaces(bitmapBuffer: Bitmap, imageRotation: Int) {

        // Detect faces in the current frame (bitmapBuffer)
        val inputImage = InputImage.fromBitmap(bitmapBuffer, imageRotation)

        val result = faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                // Task completed successfully, faces contain the list of bounding boxes enclosing faces

                for (face in faces){
                    // Get the bounding box of the image
                    val boundingBox = face.boundingBox

                    // Rotate bitmap according to imageRotation
                    val rotatedBitmap = rotateBitmap(bitmapBuffer, imageRotation.toFloat())

                    // Process bounding box information
                    var startingPointLeft = boundingBox.left
                    var width = boundingBox.width()
                    if (boundingBox.left < 0){
                        startingPointLeft = 0
                    }
                    if (boundingBox.width() + startingPointLeft > rotatedBitmap.width){
                        width = rotatedBitmap.width - startingPointLeft
                    }

                    var startingPointTop = boundingBox.top
                    Log.d("STARTING POINT TOP", startingPointTop.toString())
                    var height = boundingBox.height()
                    if (boundingBox.top < 0){
                        startingPointTop = 0
                    }
                    if (startingPointTop + boundingBox.height() > rotatedBitmap.height){
                        height = rotatedBitmap.height - startingPointTop
                    }

                    //Create cropped image to get only the face
                    val faceCropImage = Bitmap.createBitmap(rotatedBitmap, startingPointLeft, startingPointTop,
                        width, height)

                    Log.d("CAMERAFRAGMENT", "Face detected")
                    faceFinded.faceLandmarks = faces.map { detectedFace ->
                        FaceLandmarks(
                            leftEye = detectedFace.getLandmark(FaceLandmark.LEFT_EYE),
                            rightEye = detectedFace.getLandmark(FaceLandmark.RIGHT_EYE),
                            noseBase = detectedFace.getLandmark(FaceLandmark.NOSE_BASE),
                            leftEar = detectedFace.getLandmark(FaceLandmark.LEFT_EAR),
                            rightEar = detectedFace.getLandmark(FaceLandmark.RIGHT_EAR),
                            mouthLeft = detectedFace.getLandmark(FaceLandmark.MOUTH_LEFT),
                            mouthRight = detectedFace.getLandmark(FaceLandmark.MOUTH_RIGHT),
                            mouthBottom = detectedFace.getLandmark(FaceLandmark.MOUTH_BOTTOM),
                            leftCheek = detectedFace.getLandmark(FaceLandmark.LEFT_CHEEK),
                            rightCheek = detectedFace.getLandmark(FaceLandmark.RIGHT_CHEEK)
                        )
                    }
                    landmarkTSaved.leftEye = faceFinded.faceLandmarks?.get(0)?.leftEye?.position
                    landmarkTSaved.rightEye = faceFinded.faceLandmarks?.get(0)?.rightEye?.position
                    landmarkTSaved.noseBase = faceFinded.faceLandmarks?.get(0)?.noseBase?.position
                    landmarkTSaved.leftEar = faceFinded.faceLandmarks?.get(0)?.leftEar?.position
                    landmarkTSaved.rightEar = faceFinded.faceLandmarks?.get(0)?.rightEar?.position
                    landmarkTSaved.mouthLeft = faceFinded.faceLandmarks?.get(0)?.mouthLeft?.position
                    landmarkTSaved.mouthRight = faceFinded.faceLandmarks?.get(0)?.mouthRight?.position
                    landmarkTSaved.mouthBottom = faceFinded.faceLandmarks?.get(0)?.mouthBottom?.position
                    landmarkTSaved.leftCheek = faceFinded.faceLandmarks?.get(0)?.leftCheek?.position
                    landmarkTSaved.rightCheek = faceFinded.faceLandmarks?.get(0)?.rightCheek?.position


                   Log.d("CAMERAFRAGMENT FaceLandmarks",  landmarkTSaved.leftEye.toString())

                    drawLandmarks(faceCropImage, faceFinded.faceLandmarks!!)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Exception:", e.toString())
            }
    }
    fun getFaceLandmarks(): FaceLandmarkClean {
        return landmarkTSaved
    }

    fun drawLandmarks(bitmap: Bitmap, faceLandmarks: List<FaceLandmarks>) {
        val canvas = Canvas(bitmap)
        val dotPaint = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }
        val linePaint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        faceLandmarks.forEach { faceLandmark ->
            val points = listOfNotNull(
                faceLandmark.leftEye?.position,
                faceLandmark.rightEye?.position,
                faceLandmark.noseBase?.position,
                faceLandmark.leftEar?.position,
                faceLandmark.rightEar?.position,
                faceLandmark.mouthLeft?.position,
                faceLandmark.mouthRight?.position,
                faceLandmark.mouthBottom?.position,
                faceLandmark.leftCheek?.position,
                faceLandmark.rightCheek?.position
            )

            // Draw dots
            points.forEach { point ->
                canvas.drawCircle(point.x, point.y, 7f, dotPaint)
            }

            // Draw lines
            for (i in 0 until points.size - 1) {
                for (j in i + 1 until points.size) {
                    canvas.drawLine(points[i].x, points[i].y, points[j].x, points[j].y, linePaint)
                }
            }
        }
        activity?.runOnUiThread {
            fragmentCameraBinding?.faceOverlay?.setImageBitmap(bitmap)
        } ?: run {
            // Se l'activity è null, naviga al fragment DataTesting
            findNavController().navigate(R.id.action_DataCollection_to_DataTesting)
        }
    }

}