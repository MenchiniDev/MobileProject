package com.mobile.narciso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentDatatestingBinding

class DataTesting : Fragment() {

    private var _binding: FragmentDatatestingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDatatestingBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotoDataCollection.setOnClickListener {
            findNavController().navigate(R.id.action_DataTesting_to_DataCollection)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}