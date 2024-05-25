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


/**
 * DataCollection class is a Fragment used for data collection.
 * This fragment handles the display of images, the collection of user votes on the images,
 * the collection of data from the wearable device sensors and the front camera, and finally the sending of data to the database.
 *
 * It uses an ImageAdapter to display the images in a ViewPager.
 * The images are shown one at a time and the user can vote whether they like, dislike or are neutral towards the image.
 * Each time the user votes, a new set of data is requested from the wearable device sensors and data is collected from the front camera.
 *
 * The collected data includes heart, PPG and EDA sensor data from the wearable device and face landmarks from the front camera.
 * All this data, along with the user's vote and the image ID, are saved in a SensorsData object and added to a list of SensorsData.
 *
 * When the user has seen and voted on 10 images, the data is sent to the database and the user can move to the DataTesting fragment to view the results.
 */

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

    private val firebaseDataHelp = FirestoreDataDAO()

    private var imgUsed: ArrayList<String> = ArrayList()

    private var sensorsData: ArrayList<SensorsData> = ArrayList()
    //needed for firebase single data array collection
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
                Log.d("Image list", "${imageName}")
            }
            images.add(imageId)
        }
        Log.d("Data coll image", "${imgUsed[imagesSeen]}")
        MainActivity.currentImageIndex = imgUsed[imagesSeen]
        Log.d("Main activity image", "${MainActivity.currentImageIndex}")
        try{
            MainActivity.serverManager.start()
        }catch (e: Exception){
            Log.w("EEG Thread", "serverManager thread already active: $e")
        }



        // add the camera fragment to the fragment container, so we can display the camera preview
        childFragmentManager.beginTransaction().apply {
            replace(R.id.child_fragment_container, cameraFragment)
            commit()
        }
        adapter = ImageAdapter(images)

        _binding = FragmentDatacollectionBinding.inflate(inflater, container, false)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // permissions not granted: request camera permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERAPERMISSIONCODE)
        }

        //this button will appear at the end of data collecting (after 10 images seen)
        binding.goToDataTesting.visibility = View.GONE

        return binding.root
    }

    //function to change the image, called everytime a button is pressed
    private fun changeImage() {
        currentImageIndex = (currentImageIndex + 1) % images.size
        binding.viewPager.currentItem = currentImageIndex
        Log.d("New image", "$currentImageIndex")

        //saving current id on the data from the helmet
        MainActivity.currentImageIndex = imgUsed[imagesSeen]
        Log.d("Data coll new image", "${imgUsed[imagesSeen]}")
        Log.d("Main activity new image", "${MainActivity.currentImageIndex}")

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

        //sending data to database and next framgment to display graphs
        binding.goToDataTesting.setOnClickListener {

            val sessionUser = SessionManager(requireContext()).username

            if (HRsensorDataList.isNotEmpty() && PPGsensorDataList.isNotEmpty() && EDAsensorDataList.isNotEmpty()){
                var count = 0
                while(count < sensorsData.size){
                    try{
                        sensorsData[count] = sensorsData[count].copy(HearthRate = HRsensorDataList[count])
                        sensorsData[count] = sensorsData[count].copy(PPG = PPGsensorDataList[count])
                        sensorsData[count] = sensorsData[count].copy(EDA = EDAsensorDataList[count])
                    }catch (e: Exception){
                        Log.w("Watch sensors", "NOT all data got from watch, $e")
                    }
                    count++
                }
            }

            try{
                MainActivity.serverManager.stop()
            }catch (e: Exception){
                Log.d("EEG thread", "EEG thread is not active $e")
            }
            firebaseDataHelp.sendData(sessionUser!!, sensorsData, MainActivity.EEGsensordataList)
            if(!MainActivity.newTestToken){
                MainActivity.newTestToken= true
            }
            //string conversion is mandatory, Bundle doesn't accept float data
            val HRsensorDataListString = HRsensorDataList.map { it.toString() } as ArrayList<String>
            val PPGsensorDataListString = PPGsensorDataList.map { it.toString() } as ArrayList<String>
            val EDAsensorDataListString = EDAsensorDataList.map { it.toString() } as ArrayList<String>

            val bundle = Bundle()

            bundle.putStringArrayList("HRsensorDataList", HRsensorDataListString)
            bundle.putStringArrayList("PPGsensorDataList", PPGsensorDataListString)
            bundle.putStringArrayList("EDAsensorDataList", EDAsensorDataListString)

            findNavController().navigate(R.id.action_DataCollection_to_DataTesting, bundle)
        }

        //registering the receiver to get data from the watch
        val filter = IntentFilter("com.mobile.narciso.SENSOR_DATA")
        requireActivity().registerReceiver(sensorDataReceiver, filter, Context.RECEIVER_NOT_EXPORTED)

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    //function to check if the counter is at 10, if so it will show the button to go to the next fragment
    private fun checkCounter() {
        val sessionUser = SessionManager(requireContext()).username

        if (imagesSeen == 10) {
            Toast.makeText(requireContext(), "Change wifi connection to send data on cloud!", Toast.LENGTH_SHORT).show()

            binding.Beauty.visibility = View.GONE
            binding.NoBeauty.visibility = View.GONE
            binding.Neutral.visibility = View.GONE
            binding.goToDataTesting.visibility = View.VISIBLE

        }
        singleTestData = singleTestData.copy( testUser = sessionUser)
        sensorsData.add(singleTestData)
    }

    //receiver to get data from the watch
    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            HRsensorDataList.add(intent.getFloatExtra("HRsensorData", 0.0f))
            Log.d("Check data", "HR: ${HRsensorDataList}")

            PPGsensorDataList.add(intent.getFloatExtra("PPGsensorData", 0.0f))
            Log.d("Check data", "PPG: ${PPGsensorDataList}")

            EDAsensorDataList.add(intent.getFloatExtra("EDAsensorData", 0.0f))
            Log.d("Check data", "EDA: ${EDAsensorDataList}")

        }
    }

    //everytime a button is pressed we request data only from watch and save data from frontal camera
    fun takeDatafromCamWatch(Beauty: Int, faceLandmarks: FaceLandmarkClean): Boolean {
        //taking sensor data and storing in a List that will be sent to cloud and DataTesting
        val intent = Intent(requireContext(), RequestSensors::class.java)
        requireContext().startService(intent)

        //adding single landmarks group to the list
        if (faceLandmarks != null) {
            Log.d("Face data check", "Got in IF case")
            singleTestData = singleTestData.copy( faceData = faceLandmarks)
            Log.d("Face data collected", "${singleTestData.faceData}")
        }else{
            Log.d("Face data check", "Face is null")
        }
        singleTestData = singleTestData.copy( imageID = imgUsed[imagesSeen-1])  // imagesSeen already updated
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