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
import com.mobile.narciso.databinding.FragmentPasswordBinding

class Password : Fragment() {
    private var _binding: FragmentPasswordBinding? = null
    private val databaseHelper = DatabaseHelper(requireContext())
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        val email = binding.youremail.text.toString()

        binding.sendButton.setOnClickListener {
            sendResetPasswordEmail(email)
        }
        Toast.makeText(requireContext(), "creo", Toast.LENGTH_SHORT).show()

        return view
    }

    private fun sendResetPasswordEmail(email: String) {
        if (email.isNotEmpty()) {
            if (databaseHelper.checkEmailExists(email)) {
                sendEmail(email)
            } else {
                Toast.makeText(
                    requireContext(),
                    "L'indirizzo email non è registrato.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun sendEmail(email: String) {
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
            Toast.makeText(
                requireContext(),
                "Non ci sono app di email installate.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}