package com.mobile.narciso

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentFirstBinding

/**
 * FirstFragment is a Fragment that serves as the main screen of the application.
 * It checks the status of Bluetooth, camera permission, and Wi-Fi connection.
 *
 * The onCreateView method initializes the fragment and sets up the UI. It checks if Bluetooth is enabled, if camera permission is granted, and if Wi-Fi is connected.
 * If Bluetooth is not enabled, it sends an intent to enable Bluetooth.
 * If camera permission is not granted, it requests the permission.
 * The status of the Wi-Fi connection is observed using a LiveData from the `SharedViewModel`.
 *
 * The onViewCreated method sets up the click listener for the "Go to Data Collection" button. When this button is clicked, it navigates to the `DataCollection` fragment.
 *
 * The onDestroyView method is called when the view is destroyed. It sets the binding to null to avoid memory leaks.
 */

class FirstFragment : Fragment() {
    private val viewModel: SharedViewModel by activityViewModels()

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_CAMERA_PERMISSION = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        Log.d("FirstFragment","MAINACTIVITY CREATA")

        binding.textviewFirst.text = getString(R.string.presentation, SessionManager(requireContext()).username)

        binding.gotoDataCollection.isEnabled = false
        // Check Bluetooth connection
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter?.isEnabled == false) {
            try{
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }catch (e: Exception){
                Log.w("BT", "Bluetooth is not ON: $e")
            }

            binding.textviewWatchOK.setTextColor(Color.RED)
        }else
        {
            binding.textviewWatchOK.setTextColor(Color.GREEN)
        }

        // Check camera permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            binding.textviewCamOK.setTextColor(Color.RED)
        }

        //setting color GREEN if the user has already granted the permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.textviewCamOK.setTextColor(Color.GREEN)
        }

        // Check wifi connection
        viewModel.isWifiConnected.observe(viewLifecycleOwner) { isWifiConnected ->
            if (isWifiConnected) {
                binding.textviewHelmetOK.setTextColor(Color.GREEN)
                binding.textviewHelmetOK.text = "Helmet connected"
                binding.gotoDataCollection.isEnabled = true
                binding.radioButton.visibility = View.GONE
            } else {
                binding.textviewHelmetOK.setTextColor(Color.RED)
                binding.textviewHelmetOK.text = "Helmet not connected"
                binding.gotoDataCollection.isEnabled = false
                binding.radioButton.visibility = View.VISIBLE
            }
        }

        if(MainActivity.newTestToken){
            MainActivity.EEGsensordataList.clear()
        }


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