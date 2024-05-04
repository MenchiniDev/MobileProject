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
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class DataCollection : Fragment() {

    private var _binding: FragmentDatacollectionBinding? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private val CAMERAPERMISSIONCODE = 1001

    //code needed to connect data COllection to the image adapter
    //val images = listOf(R.drawable.n001, R.drawable.a001, R.drawable.a002)

    private val binding get() = _binding!!
    private var currentImageIndex = 0
    private var adapter = ImageAdapter(listOf())
    val images = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            // Proceed with camera operations
            // E.g., openCamera()
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERAPERMISSIONCODE)
        }

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

        //CHANGE: added the following code to navigate to the next fragment after some image analysis
        /*binding.gotoDataTestingDATACOLLECTION.setOnClickListener {
            Toast.makeText(requireContext(), "sto andando a data testing!", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigate(R.id.action_DataCollection_to_DataTesting)
        }*/


        binding.Beauty.setOnClickListener {
            changeImage()
        }

        binding.NoBeauty.setOnClickListener {
            changeImage()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
