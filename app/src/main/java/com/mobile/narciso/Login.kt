package com.mobile.narciso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mobile.narciso.databinding.FragmentLoginBinding

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val CAMERA_PERMISSION_CODE = 1001

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.login.setOnClickListener()
        {
            Toast.makeText(requireContext(), "login", Toast.LENGTH_SHORT).show()
        }

        binding.signup.setOnClickListener()
        {
            Toast.makeText(requireContext(), "signup", Toast.LENGTH_SHORT).show()
        }

        binding.forgotPassword.setOnClickListener()
        {
            Toast.makeText(requireContext(), "forgot password", Toast.LENGTH_SHORT).show()
        }
    }
}