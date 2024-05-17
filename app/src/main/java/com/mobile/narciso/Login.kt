package com.mobile.narciso

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentLoginBinding
import kotlinx.coroutines.runBlocking

class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Creazione di un'istanza di SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Creazione di un'istanza di DatabaseHelper
        val databaseHelper = DatabaseHelper(requireContext())
        val firebaseHelpAccount = FirestoreAccountDAO()

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inizializzazione di SessionManager
        sessionManager = SessionManager(requireContext())

        binding.signup.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        binding.login.setOnClickListener {
            val username = binding.editTextTextUsername.text.toString()
            val email = binding.editTextTextEmailAddress.text.toString()
            val password = binding.editTextTextPassword.text.toString()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // val isInserted = databaseHelper.checkUser(username, email, password) // Modifica questa linea
                val allowLogin = runBlocking { firebaseHelpAccount.checkAccount(username, email, password) }

                if (allowLogin) {
                    // Salvataggio del nome dell'utente nella variabile di sessione
                    sessionManager.username = username

                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_FirstFragment)
                } else {
                    Toast.makeText(requireContext(), "Login failed!", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.forgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_password)
        }
        return view
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}