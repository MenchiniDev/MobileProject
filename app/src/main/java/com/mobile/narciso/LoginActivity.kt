package com.mobile.narciso

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.mobile.narciso.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        //binding = LoginActivity.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener(){
        Toast.makeText(this, "login", Toast.LENGTH_SHORT).show()
        }

        binding.forgotPassword.setOnClickListener()
        {
            Toast.makeText(this,"forgotpassword",Toast.LENGTH_SHORT).show()
        }


        binding.signup.setOnClickListener(){
            Toast.makeText(this, "signup", Toast.LENGTH_SHORT).show()
        }
    }
}