package com.isi.passwordgame.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.isi.passwordgame.databinding.HomeLayoutBinding

class HomeActivity : ComponentActivity() {
    private lateinit var binding: HomeLayoutBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email")
        val displayName = intent.getStringExtra("name")

        binding.Name.setText(displayName)
        binding.Email.setText(email)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            binding.uid.setText(currentUser.uid)
        }
    }
}

