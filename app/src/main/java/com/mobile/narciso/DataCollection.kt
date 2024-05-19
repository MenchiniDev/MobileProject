package com.mobile.narciso

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mobile.narciso.databinding.FragmentDatacollectionBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DataCollection : Fragment() {

    private val viewModel: SharedViewModel by activityViewModels()

    private var _binding: FragmentDatacollectionBinding? = null
    private lateinit var cameraExecutor: ExecutorService
    private val CAMERAPERMISSIONCODE = 1001
    private lateinit var bitmapBuffer: Bitmap

    private val LIKEVALUE = 1
    private val NEUTRALVALUE = 0
    private val DONTLIKEVALUE = -1

    private val cameraFragment = CameraFragment()

    private val binding get() = _binding!!
    private var currentImageIndex = 0
    private var adapter = ImageAdapter(listOf())
    val images = mutableListOf<Int>()
    private var imagesSeen = 0

    //data lists
    private var HRsensorDataList: ArrayList<Float> = ArrayList()
    private var PPGsensorDataList: ArrayList<Float> = ArrayList()
    private var EDAsensorDataList: ArrayList<Float> = ArrayList()

    //array of array of facelandmarks: the first iterator moves throug different data
    //the second selects the single face's parts of a single istance of data
    private var FaceLandmarksList:  ArrayList<List<FaceLandmarks>> = ArrayList()

    private var imgUsed: ArrayList<String> = ArrayList()

    private var sensorsData: ArrayList<SensorsData> = ArrayList()
    private var singleTestData: SensorsData = SensorsData()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // create a list of random image names to display
        val imageNames = (1..479).map { String.format("a%03d", it) }.shuffled()

        // get the resource id for each image name
        var count = 0
        for (imageName in imageNames) {
            val imageId = resources.getIdentifier(imageName, "drawable", "com.mobile.narciso")
            if(count < 11){
                imgUsed.add(imageName)
                count++
            }
            images.add(imageId)
        }
        MainActivity.currentImageIndex = imgUsed[imagesSeen]
        MainActivity.serverManager.start()



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

        //saving current id on the data from the helmet
        MainActivity.currentImageIndex = imgUsed[imagesSeen]
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
            imagesSeen++
            changeImage()

            //saving the current vote in the list of helmet's data
            MainActivity.currentVote = LIKEVALUE
            takeDatafromCamWatch(LIKEVALUE,cameraFragment.getFaceLandmarks())
            checkCounter()
        }

        binding.Neutral.setOnClickListener {
            imagesSeen++
            changeImage()

            //saving the current vote in the list of helmet's data
            MainActivity.currentVote = NEUTRALVALUE
            takeDatafromCamWatch(NEUTRALVALUE,cameraFragment.getFaceLandmarks())
            checkCounter()
        }

        binding.NoBeauty.setOnClickListener {
            imagesSeen++
            changeImage()

            //saving the current vote in the list of helmet's data
            MainActivity.currentVote = DONTLIKEVALUE
            takeDatafromCamWatch(DONTLIKEVALUE, cameraFragment.getFaceLandmarks())
            checkCounter()
        }

        //invio i dati al fragment Datatesting (Result)
        binding.goToDataTesting.setOnClickListener {


            MainActivity.serverManager.stop()

            val firebaseDataHelp = FirestoreDataDAO()
            val sessionUser = SessionManager(requireContext()).username
            firebaseDataHelp.sendData(sessionUser!!, sensorsData, MainActivity.EEGsensordataList)

            //string conversion is mandatory, Bundle doesn't accept float data
            val HRsensorDataListString = HRsensorDataList.map { it.toString() } as ArrayList<String>
            val PPGsensorDataListString = PPGsensorDataList.map { it.toString() } as ArrayList<String>
            val EDAsensorDataListString = EDAsensorDataList.map { it.toString() } as ArrayList<String>
            //MainActivity.EEGsensordataList da mandare al cloud

            val bundle = Bundle()

            bundle.putStringArrayList("HRsensorDataList", HRsensorDataListString)
            bundle.putStringArrayList("PPGsensorDataList", PPGsensorDataListString)
            bundle.putStringArrayList("EDAsensorDataList", EDAsensorDataListString)

            findNavController().navigate(R.id.action_DataCollection_to_DataTesting, bundle)
        }

        val filter = IntentFilter("com.mobile.narciso.SENSOR_DATA")
        requireActivity().registerReceiver(sensorDataReceiver, filter, Context.RECEIVER_NOT_EXPORTED)


        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    private fun checkCounter() {
        val sessionUser = SessionManager(requireContext()).username

        if (imagesSeen == 10) {
            Toast.makeText(requireContext(), "Change wifi connection to send data on cloud!", Toast.LENGTH_SHORT).show()

            binding.Beauty.visibility = View.GONE
            binding.NoBeauty.visibility = View.GONE
            binding.Neutral.visibility = View.GONE
            binding.goToDataTesting.visibility = View.VISIBLE

            singleTestData = singleTestData.copy( testUser = sessionUser)
            sensorsData.add(singleTestData)

        }else{

            singleTestData = singleTestData.copy( testUser = sessionUser)
            sensorsData.add(singleTestData)
        }
    }

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            HRsensorDataList.add(intent.getFloatExtra("HRsensorData", 0.0f))
            singleTestData = singleTestData.copy( HearthRate = intent.getFloatExtra("HRsensorData", 0.0f))
            Log.d("Check data", "HR: ${singleTestData?.HearthRate}")

            PPGsensorDataList.add(intent.getFloatExtra("PPGsensorData", 0.0f))
            singleTestData = singleTestData.copy( PPG = intent.getFloatExtra("PPGsensorData", 0.0f))
            Log.d("Check data", "PPG: ${singleTestData?.PPG}")

            EDAsensorDataList.add(intent.getFloatExtra("EDAsensorData", 0.0f))
            singleTestData = singleTestData.copy( EDA = intent.getFloatExtra("EDAsensorData", 0.0f))
            Log.d("Check data", "EDA: ${singleTestData?.EDA}")


        }
    }

    fun takeDatafromCamWatch(Beauty: Int, faceLandmarks: List<FaceLandmarks>?): Boolean {
        //taking sensor data and storing in a List that will be sento to cloud and DataTesting
        val intent = Intent(requireContext(), RequestSensors::class.java)
        requireContext().startService(intent)
        //Toast.makeText(requireContext(), "SEND DATA: DATI RICEVUTI!", Toast.LENGTH_SHORT).show()
        faceLandmarks?.forEach { faceLandmarks ->
            Log.d("left eye","Left Eye: ${faceLandmarks.leftEye}")
            Log.d("right eye","Right Eye: ${faceLandmarks.rightEye}")
            Log.d("nose base","Nose Base: ${faceLandmarks.noseBase}")
            Log.d("left ear","Left Ear: ${faceLandmarks.leftEar}")
            Log.d("right ear","Right Ear: ${faceLandmarks.rightEar}")
            Log.d("mouth left","Mouth Left: ${faceLandmarks.mouthLeft}")
            Log.d("mouth right","Mouth Right: ${faceLandmarks.mouthRight}")
            Log.d("mouth bottom","Mouth Bottom: ${faceLandmarks.mouthBottom}")
            Log.d("left cheek","Left Cheek: ${faceLandmarks.leftCheek}")
            Log.d("right cheek","Right Cheek: ${faceLandmarks.rightCheek}")
        }

        //adding single landmarks group to the list
        if (faceLandmarks != null) {
            Log.d("Face data chech", "Got in IF case")
            FaceLandmarksList.add(faceLandmarks)
            singleTestData = singleTestData.copy( faceData = faceLandmarks.first())
        }
        singleTestData = singleTestData.copy( imageID = imgUsed[imagesSeen])
        singleTestData = singleTestData.copy( likability = Beauty)


        return true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(sensorDataReceiver)
        _binding = null
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