package com.isi.passwordgame

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.isi.passwordgame.databinding.LoginLayoutBinding

class LoginActivity : ComponentActivity() {
    private lateinit var binding: LoginLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        initLoginLayout()

    }

    private fun initLoginLayout() {
        val loginAnimation = binding.loginLayout.background as AnimationDrawable
        loginAnimation.apply {
            setEnterFadeDuration(1000)
            setExitFadeDuration(2000)
            start()
        }
    }
}

