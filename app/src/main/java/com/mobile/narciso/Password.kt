package com.mobile.narciso

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mobile.narciso.databinding.FragmentLoginBinding
import java.net.Authenticator
import java.net.PasswordAuthentication
import java.util.Properties

class Password : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    val email = binding.editTextTextEmailAddress.text.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    fun sendResetPasswordEmail(recipientEmail: String) {
        if (email.isNotEmpty()) {
            val mailto = "mailto:$email" +
                    "?cc=" + "" +
                    "&subject=" + Uri.encode("Reimpostazione della password") +
                    "&body=" + Uri.encode("Clicca sul link per reimpostare la password.")

            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse(mailto)

            try {
                startActivity(emailIntent)
                findNavController().navigate(R.id.action_password_to_login)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "Non ci sono app di email installate.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Inserisci un indirizzo email.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}