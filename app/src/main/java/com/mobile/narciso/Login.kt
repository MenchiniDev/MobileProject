package com.mobile.narciso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentLoginBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.*
import androidx.lifecycle.ViewModelProvider
import com.mobile.narciso.User

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    val user = MutableLiveData<User>()
    val loginTime = MutableLiveData<Date>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Creazione di un'istanza di DatabaseHelper
        val databaseHelper = DatabaseHelper(requireContext())
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        var viewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        binding.signup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        binding.login.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val isInserted = databaseHelper.checkUser(email, password)
                if (isInserted) {
                    // Osservazione del LiveData authenticatedUser per sapere quando viene aggiornato
                    val observer = Observer<User> { user ->
                        // Verifica se l'utente è stato autenticato correttamente
                        findNavController().navigate(R.id.action_loginFragment_to_FirstFragment)
                    }
                    viewModel.authenticatedUser.observe(viewLifecycleOwner, observer)
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(requireContext(), "Registration failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT)
                    .show()
            }
            binding.forgotPassword.setOnClickListener {
                // Non implementato
            }
        }
        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
