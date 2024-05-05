package com.mobile.narciso

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
        val databaseHelper = DatabaseHelper(requireContext())
        if (email.isNotEmpty()) {
            if (databaseHelper.checkEmailExists(email)) {
                //the user exists, send the email with the new password
                val newpass = databaseHelper.resetPassword(email)
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