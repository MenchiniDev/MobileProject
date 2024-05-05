package com.mobile.narciso

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_CAMERA_PERMISSION = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        binding.textviewFirst.text = getString(R.string.presentation, SessionManager(requireContext()).username)

        binding.gotoDataCollection.isEnabled = false
        // Check Bluetooth connection
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            binding.textviewWatchOK.setTextColor(Color.RED)
        }else
        {
            binding.textviewWatchOK.setTextColor(Color.GREEN)
        }

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            binding.textviewCamOK.setTextColor(Color.RED)
        }else
        {
            binding.textviewCamOK.setTextColor(Color.GREEN)
        }

        // Set helmet textview to red by default
        binding.textviewHelmetOK.setTextColor(Color.RED)

        // Set radio button listener
        binding.radioButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.textviewHelmetOK.setTextColor(Color.GREEN)
                binding.gotoDataCollection.isEnabled = true
            } else {
                binding.textviewHelmetOK.setTextColor(Color.RED)
                binding.gotoDataCollection.isEnabled = false
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotoDataCollection.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_DataCollection)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}