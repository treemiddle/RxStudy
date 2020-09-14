package com.jay.rxstudyfirst.view.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.jay.rxstudyfirst.R
import com.jay.rxstudyfirst.databinding.ActivityLoginBinding
import com.jay.rxstudyfirst.view.signin.SignInActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        initClickListener()
    }

    private fun initClickListener() {
        with(binding) {
            btnLogin.setOnClickListener {

            }
            btnSignin.setOnClickListener {
                goSignIn()
            }
        }
    }

    private fun goSignIn() {
        startActivity(Intent(this, SignInActivity::class.java))
    }
}