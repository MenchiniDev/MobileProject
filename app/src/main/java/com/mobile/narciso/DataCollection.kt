package com.mobile.narciso

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mobile.narciso.databinding.FragmentDatacollectionBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DataCollection : Fragment() {

    private var _binding: FragmentDatacollectionBinding? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    private val CAMERA_PERMISSION_CODE = 1001
    private var currentImageIndex = 0;


    //code needed to connect data COllection to the image adapter
    val images = listOf(R.drawable.n001, R.drawable.a001, R.drawable.a002)
    val adapter = ImageAdapter(images)

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDatacollectionBinding.inflate(inflater, container, false)
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            // Proceed with camera operations
            // E.g., openCamera()
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }

        return binding.root
    }
    private fun changeImage() {
        currentImageIndex = (currentImageIndex + 1) % images.size
        binding.viewPager.currentItem = currentImageIndex
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = adapter

        binding.gotoDataTestingDATACOLLECTION.setOnClickListener {
            Toast.makeText(requireContext(), "sto andando a data testing!", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigate(R.id.action_DataCollection_to_DataTesting)
        }

        binding.gotoCamera.setOnClickListener {
            findNavController().navigate(R.id.action_DataCollection_to_camera)
        }


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
