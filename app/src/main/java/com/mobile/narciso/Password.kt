package com.mobile.narciso

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mobile.narciso.databinding.FragmentPasswordBinding
import kotlinx.coroutines.runBlocking

/**
 * Password is a Fragment that handles the password reset process.
 * It interacts with Firestore to reset the user's password.
 *
 * The onCreateView method initializes the fragment and sets up the UI. It sets up a click listener for the send button.
 * When the send button is clicked, it retrieves the email from the input field, checks if it is not empty, and then calls the sendResetPasswordEmail method to reset the user's password.
 *
 * The sendResetPasswordEmail method checks if the email exists in the Firestore database using the checkEmailExists method from FirestoreAccountDAO.
 * If the email exists, it resets the password using the resetPassword method from FirestoreAccountDAO, and then calls the popUpPass method to display the new password.
 * If the email does not exist or if the input field is empty, it displays an error message.
 *
 * The popUpPass method displays the new password and copies it to the clipboard.
 *
 * The onDestroyView method is called when the view is destroyed. It sets the binding to null to avoid memory leaks.
 *
 * This fragment is part of an application that collects and analyzes sensor data for research purposes.
 */

class Password : Fragment() {
    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.sendButton.setOnClickListener {
            val email = binding.youremail.text.toString()
            sendResetPasswordEmail(email)
        }

        return view
    }

    private fun sendResetPasswordEmail(email: String) {
        //creating an istance of DataBaseHelper to query database

        // val databaseHelper = DatabaseHelper(requireContext())
        val firebaseHelpAccount = FirestoreAccountDAO()

        if (email.isNotEmpty()) {
            if (runBlocking { firebaseHelpAccount.checkEmailExists(email) }) {   // with SQLite use databaseHelper.checkEmailExists(email)
                //the user exists, send the email with the new password
                val newpass = runBlocking { firebaseHelpAccount.resetPassword(email) }

                popUpPass(email,newpass)
            } else {
                Toast.makeText(
                    requireContext(),
                    "L'indirizzo email non è registrato.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }else{
            Toast.makeText(
                requireContext(),
                "Inserisci un indirizzo email.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun popUpPass(email: String,newpass: String) {
        binding.newpassView.text = "La tua nuova password è: $newpass"

        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", newpass)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "Password copiata negli appunti", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}