package com.mobile.narciso

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentDatacollectionBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class DataCollection : Fragment() {

    private var _binding: FragmentDatacollectionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDatacollectionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gotoDataTesting.setOnClickListener {
            findNavController().navigate(R.id.action_DataCollection_to_DataTesting)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}