package com.mobile.narciso

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mobile.narciso.databinding.FragmentDatacollectionBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class DataCollection : Fragment() {

    private var _binding: FragmentDatacollectionBinding? = null
    private lateinit var cameraExecutor: ExecutorService
    private val CAMERAPERMISSIONCODE = 1001
    private val binding get() = _binding!!
    private var currentImageIndex = 0
    private var adapter = ImageAdapter(listOf())
    val images = mutableListOf<Int>()
    private var imagesSeen = 0

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
        //ADD: aggiungere il codice per terminare le iterazioni dopo X immagini
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

        //TODO CHANGE: added the following code to navigate to the next fragment after some image analysis
        /*binding.gotoDataTestingDATACOLLECTION.setOnClickListener {
            Toast.makeText(requireContext(), "sto andando a data testing!", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigate(R.id.action_DataCollection_to_DataTesting)
        }*/


        binding.Beauty.setOnClickListener {
            changeImage()
            sendData(true)
            imagesSeen++
            checkCounter()
        }

        binding.NoBeauty.setOnClickListener {
            changeImage()
            sendData(false)
            imagesSeen++
            checkCounter()
        }
        binding.goToDataTesting.setOnClickListener {
            findNavController().navigate(R.id.action_DataCollection_to_DataTesting)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
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
        _binding = null
    }

    fun sendData(Beauty: Boolean): Boolean {
        //TODO: implementare il codice per inviare i dati al server
        return Beauty
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
