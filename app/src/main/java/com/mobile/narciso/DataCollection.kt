package com.mobile.narciso

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.common.InputImage
import com.mobile.narciso.databinding.FragmentDatacollectionBinding
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class DataCollection : Fragment() {

    private var _binding: FragmentDatacollectionBinding? = null
    private lateinit var cameraExecutor: ExecutorService
    private val CAMERAPERMISSIONCODE = 1001
    private lateinit var bitmapBuffer: Bitmap

    private val binding get() = _binding!!
    private var currentImageIndex = 0
    private var adapter = ImageAdapter(listOf())
    val images = mutableListOf<Int>()
    private var imagesSeen = 0

    private var HRsensorDataList: ArrayList<Float> = ArrayList()
    private var ECGsensorDataList: ArrayList<Float> = ArrayList()
    private var PPGsensorDataList: ArrayList<Float> = ArrayList()

    //photo and saving
    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // create a list of random image names to display
        val imageNames = (1..440).map { String.format("a%03d", it) }.shuffled()

        // get the resource id for each image name
        for (imageName in imageNames) {
            val imageId = resources.getIdentifier(imageName, "drawable", "com.mobile.narciso")
            images.add(imageId)
        }

        // add the camera fragment to the fragment container
        val cameraFragment = CameraFragment() // Crea una nuova istanza del tuo CameraFragment
        childFragmentManager.beginTransaction().apply {
            replace(R.id.child_fragment_container, cameraFragment)
            commit()
        }
        adapter = ImageAdapter(images)

        _binding = FragmentDatacollectionBinding.inflate(inflater, container, false)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permissions not granted: request camera permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERAPERMISSIONCODE)
        }

        binding.goToDataTesting.visibility = View.GONE

        return binding.root
    }



    private fun changeImage() {
        currentImageIndex = (currentImageIndex + 1) % images.size
        binding.viewPager.currentItem = currentImageIndex
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = adapter

        binding.viewPager.apply {
            adapter = ImageAdapter(images)

            // Disable swipe
            (getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            isUserInputEnabled = false
        }

        binding.Beauty.setOnClickListener {
            takePhoto()
            changeImage()
            sendData(true)
            imagesSeen++
            checkCounter()
        }

        binding.NoBeauty.setOnClickListener {
            takePhoto()
            changeImage()
            sendData(false)
            imagesSeen++
            checkCounter()
        }

        //invio i dati al fragment Datatesting (Result)
        binding.goToDataTesting.setOnClickListener {

            Toast.makeText(requireContext(), "sto andando a data testing!", Toast.LENGTH_SHORT).show()

            //string conversion is mandatory, Bundle doesn't accept float data
            val HRsensorDataListString = HRsensorDataList.map { it.toString() } as ArrayList<String>
            val ECGsensorDataListString = HRsensorDataList.map { it.toString() } as ArrayList<String>
            val PPGsensorDataListString = HRsensorDataList.map { it.toString() } as ArrayList<String>

            val bundle = Bundle()

            bundle.putStringArrayList("HRsensorDataList", HRsensorDataListString)
            bundle.putStringArrayList("ECGsensorDataList", ECGsensorDataListString)
            bundle.putStringArrayList("PPGsensorDataList", PPGsensorDataListString)
            findNavController().navigate(R.id.action_DataCollection_to_DataTesting, bundle)
        }

        val filter = IntentFilter("com.mobile.narciso.SENSOR_DATA")
        requireActivity().registerReceiver(sensorDataReceiver, filter, Context.RECEIVER_NOT_EXPORTED)


        cameraExecutor = Executors.newSingleThreadExecutor()
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

    private fun takePhoto() {
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        // Ottieni un file di output per salvare l'immagine
        val imageFile = createImageFile()

        // Configura le opzioni di output per l'immagine
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(imageFile).build()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Discard frames until the processing of the previous one is not completed
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
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

                    saveBitmapToFile(bitmapBuffer, imageFile)
                    val imageRotation = image.imageInfo.rotationDegrees

                    //detectFaces(bitmapBuffer,imageRotation)
                }
            }

        // Select front camera as a default
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    }

    /*private fun detectFaces(bitmapBuffer: Bitmap, imageRotation: Int) {

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


                    //Feed the deep learning model with the cropped image
                    //emotionRecognizer.detect(faceCropImage)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Exception:", e.toString())
            }
    }*/

    private fun getGalleryPath(): String {
        Toast.makeText(requireContext(),"${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DCIM}/Narciso" , Toast.LENGTH_SHORT).show()
        return "${Environment.getExternalStorageDirectory()}/${Environment.DIRECTORY_DCIM}/Narciso"
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = File(getGalleryPath())
        return File.createTempFile(
            "PHOTO_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    private fun checkCounter()
    {
        if (imagesSeen == 10) {
            binding.Beauty.visibility = View.GONE
            binding.NoBeauty.visibility = View.GONE
            binding.goToDataTesting.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(sensorDataReceiver)
        _binding = null
    }

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            HRsensorDataList.add(intent.getFloatExtra("HRsensorData", 0.0f))
            ECGsensorDataList.add(intent.getFloatExtra("ECGsensorData", 0.0f))
            PPGsensorDataList.add(intent.getFloatExtra("PPGsensorData", 0.0f))
            Log.d("HRsensorData", intent.getFloatExtra("HRsensorData", 0.0f).toString())
            Log.d("ECGsensorData", intent.getFloatExtra("ECGsensorData", 0.0f).toString())
            Log.d("PPGsensorData", intent.getFloatExtra("PPGsensorData", 0.0f).toString())
        }
    }

    fun sendData(Beauty: Boolean): Boolean {
        //TODO: implementare il codice per inviare i dati al cloud
        //taking sensor data and storing in a List that will be sento to cloud and DataTesting
        val intent = Intent(requireContext(), RequestSensors::class.java)
        requireContext().startService(intent)

        return true
    }
}

class ImageAdapter(private val images: List<Int>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
       val imageView: ImageView = itemView.findViewById(R.id.image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.imageView.setImageResource(images[position])
    }

    override fun getItemCount(): Int {
        return images.size
    }

}
