package com.example.mealmatch

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val signUpText = findViewById<TextView>(R.id.sign_up_text)
        val loginBtn = findViewById<Button>(R.id.login_btn)
        val backText = findViewById<TextView>(R.id.back_text)

        val emailBox = findViewById<EditText>(R.id.email_box)
        val passwordBox = findViewById<EditText>(R.id.password_box)
        val eyeBtn = findViewById<ImageView>(R.id.eye_btn)

        // ---- Go to SignUpActivity ----
        signUpText.isClickable = true
        signUpText.isFocusable = true
        signUpText.bringToFront()

        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // ---- Back to MainActivity ----
        backText.setOnClickListener {
            finish()
        }

        // ---- Login (placeholder for Firebase login) ----
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

            // After Firebase login later:
            // startActivity(Intent(this, MainActivity::class.java))
            // finish()
        }

        // ---- Password visibility toggle ----
        var isPasswordVisible = false
        eyeBtn.setImageResource(R.drawable.eye_ic)

        eyeBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordBox.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                eyeBtn.setImageResource(R.drawable.ic_eye_off)
            } else {
                passwordBox.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeBtn.setImageResource(R.drawable.eye_ic)
            }

            passwordBox.setSelection(passwordBox.text.length)
        }
    }
}