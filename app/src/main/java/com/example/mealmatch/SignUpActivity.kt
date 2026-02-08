package com.example.mealmatch

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    private var passwordVisible = false
    private var rePasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // --- Views ---
        val loginText = findViewById<TextView>(R.id.sign_in_text)   // "Log in"
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)

        val nameBox = findViewById<EditText>(R.id.full_name_box)
        val emailBox = findViewById<EditText>(R.id.email_box)
        val passBox = findViewById<EditText>(R.id.password_box)
        val rePassBox = findViewById<EditText>(R.id.re_password_box)

        val passwordEye = findViewById<ImageView>(R.id.password_eye)
        val rePasswordEye = findViewById<ImageView>(R.id.re_password_eye)

        // --- Initial state: password hidden -> show eye_off icon ---
        passwordEye.setImageResource(R.drawable.eye_ic)
        rePasswordEye.setImageResource(R.drawable.eye_ic)

        // --- Nav: back to Login ---
        loginText.setOnClickListener { finish() }

        // --- Toggle password visibility (and icon) ---
        passwordEye.setOnClickListener {
            passwordVisible = !passwordVisible
            togglePassword(passBox, passwordEye, passwordVisible)
        }

        rePasswordEye.setOnClickListener {
            rePasswordVisible = !rePasswordVisible
            togglePassword(rePassBox, rePasswordEye, rePasswordVisible)
        }

        // --- Sign Up ---
        signUpBtn.setOnClickListener {
            val name = nameBox.text.toString().trim()
            val email = emailBox.text.toString().trim()
            val pass = passBox.text.toString().trim()
            val rePass = rePassBox.text.toString().trim()

            if (name.isEmpty()) {
                nameBox.error = "Name required"
                nameBox.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailBox.error = "Email required"
                emailBox.requestFocus()
                return@setOnClickListener
            }

            if (pass.isEmpty()) {
                passBox.error = "Password required"
                passBox.requestFocus()
                return@setOnClickListener
            }

            if (rePass.isEmpty()) {
                rePassBox.error = "Re-enter password"
                rePassBox.requestFocus()
                return@setOnClickListener
            }

            if (pass != rePass) {
                rePassBox.error = "Passwords do not match"
                rePassBox.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun togglePassword(editText: EditText, eye: ImageView, visible: Boolean) {
        if (visible) {
            // show text
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            eye.setImageResource(R.drawable.ic_eye_off) // 👁 visible
        } else {
            // hide text (dots)
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            eye.setImageResource(R.drawable.eye_ic)
        }

        // keep cursor at end
        editText.setSelection(editText.text?.length ?: 0)
    }
}
