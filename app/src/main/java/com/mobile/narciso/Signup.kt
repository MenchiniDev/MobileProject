package com.mobile.narciso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentSignupBinding
import kotlinx.coroutines.runBlocking

class Signup : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val view = binding.root

        // val databaseHelper = DatabaseHelper(requireContext())
        val firebaseHelpAccount = FirestoreAccountDAO()

        binding.signup.setOnClickListener {

            val email = binding.editTextTextEmailAddress.text.toString()
            val user = binding.editTextTextUsername.text.toString()
            val password = binding.editTextTextPassword.text.toString()
            var passwordrepeted = binding.editTextTextPassword2.text.toString()

            val passwordmatch = password == passwordrepeted

            if(!passwordmatch)
            {
                Toast.makeText(requireContext(),"Le password non corrispondono", Toast.LENGTH_SHORT).show()
                binding.signup.isEnabled = true
            }

            if (email.isNotEmpty() && password.isNotEmpty() && passwordrepeted.isNotEmpty() && user.isNotEmpty()) {
                // val isInserted = databaseHelper.addUser(user, email , password)
                val isInserted = runBlocking { firebaseHelpAccount.addUser(user, email, password) }
                if (isInserted == 1) {
                    //insert happened, going to login
                    Toast.makeText(requireContext(), "User registered successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signup_to_login)
                } else if(isInserted == -1) {
                    // insert failed due to network problems
                    Toast.makeText(requireContext(), "Registration failed!", Toast.LENGTH_SHORT).show()
                } else if(isInserted == 0){
                    // insert failed beacuse user already registered
                    Toast.makeText(requireContext(), "User already exists!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
