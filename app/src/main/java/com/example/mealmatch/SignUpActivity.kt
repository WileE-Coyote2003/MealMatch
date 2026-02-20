package com.example.mealmatch

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private val TAG = "SignUpActivity"

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var passwordVisible = false
    private var rePasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        //IMPORTANT: initialize Firebase BEFORE getInstance()
        FirebaseApp.initializeApp(this)
        Log.d(TAG, "FirebaseApp initialized")

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // --- Views ---
        val loginText = findViewById<TextView>(R.id.sign_in_text)   // "Log in"
        val signUpBtn = findViewById<Button>(R.id.sign_up_btn)

        val nameBox = findViewById<EditText>(R.id.full_name_box)
        val emailBox = findViewById<EditText>(R.id.email_box)
        val passBox = findViewById<EditText>(R.id.password_box)
        val rePassBox = findViewById<EditText>(R.id.re_password_box)

        val passwordEye = findViewById<ImageView>(R.id.password_eye)
        val rePasswordEye = findViewById<ImageView>(R.id.re_password_eye)

        // --- Initial state: password hidden -> show eye icon ---
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

            Log.d(TAG, "Starting signup for email=$email")
            signUpBtn.isEnabled = false

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid == null) {
                        Log.e(TAG, "Signup succeeded but UID is null")
                        signUpBtn.isEnabled = true
                        Toast.makeText(this, "Signup failed (UID missing)", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    Log.d(TAG, "Firebase Auth success. uid=$uid")

                    val userData = hashMapOf(
                        "name" to name,
                        "email" to email,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "score" to 0
                    )

                    db.collection("users").document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Account created", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            signUpBtn.isEnabled = true
                            Toast.makeText(this, e.message ?: "Firestore error", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    signUpBtn.isEnabled = true
                    Toast.makeText(this, e.message ?: "Auth error", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun togglePassword(editText: EditText, eye: ImageView, visible: Boolean) {
        if (visible) {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            eye.setImageResource(R.drawable.ic_eye_off)
        } else {
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            eye.setImageResource(R.drawable.eye_ic)
        }
        editText.setSelection(editText.text?.length ?: 0)
    }
}