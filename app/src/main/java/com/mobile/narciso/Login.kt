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

/**
 * Login is a Fragment that handles the user login process.
 * It interacts with Firestore to authenticate users.
 *
 * The onCreateView method initializes the fragment and sets up the UI. It sets up click listeners for the login, sign up, and forgot password buttons.
 * When the login button is clicked, it retrieves the username, email, and password from the input fields, checks if they are not empty, and then calls the checkAccount method from FirestoreAccountDAO to verify the user's credentials.
 * If the credentials are valid, it saves the username to the session using SessionManager, displays a success message, and navigates to the FirstFragment.
 * If the credentials are not valid or if any of the input fields are empty, it displays an error message.
 * When the sign up button is clicked, it navigates to the sign up screen.
 * When the forgot password button is clicked, it navigates to the password reset screen.
 */


class Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Creazione di un'istanza di SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // create firebase istance
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
                val allowLogin = runBlocking { firebaseHelpAccount.checkAccount(username, email, password) }

                if (allowLogin) {
                    // saving user data to the session
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