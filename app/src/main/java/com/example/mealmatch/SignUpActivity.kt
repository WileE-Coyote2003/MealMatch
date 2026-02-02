package com.example.mealmatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val loginText = findViewById<TextView>(R.id.sign_in_text) // "Log in"
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)

        val nameBox = findViewById<EditText>(R.id.full_name_box)
        val emailBox = findViewById<EditText>(R.id.email_box)
        val passBox = findViewById<EditText>(R.id.password_box)
        val rePassBox = findViewById<EditText>(R.id.re_password_box)

        loginText.setOnClickListener {
            finish() // just go back to LoginActivity (cleaner)
        }

        signUpBtn.setOnClickListener {
            val name = nameBox.text.toString().trim()
            val email = emailBox.text.toString().trim()
            val pass = passBox.text.toString().trim()
            val rePass = rePassBox.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || rePass.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != rePass) {
                rePassBox.error = "Passwords do not match"
                rePassBox.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()

            // After signup go back to login screen
            finish()
        }
    }
}
