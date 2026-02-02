package com.example.mealmatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val signUpText = findViewById<TextView>(R.id.sign_in_text) // "Sign Up"
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val backText = findViewById<TextView>(R.id.back_text)

        val emailBox = findViewById<EditText>(R.id.email_box)
        val passwordBox = findViewById<EditText>(R.id.password_box)

        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        backText.setOnClickListener {
            finish()
        }

        loginBtn.setOnClickListener {
            val email = emailBox.text.toString().trim()
            val password = passwordBox.text.toString().trim()

            if (email.isEmpty()) {
                emailBox.error = "Email required"
                emailBox.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordBox.error = "Password required"
                passwordBox.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

            // Example after real login:
            // startActivity(Intent(this, HomeActivity::class.java))
            // finish()
        }
    }
}
