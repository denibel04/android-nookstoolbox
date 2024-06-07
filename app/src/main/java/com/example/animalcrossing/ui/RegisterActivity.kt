package com.example.animalcrossing.ui

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.ActivityLoginBinding
import com.example.animalcrossing.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val buttonRegister = binding.register
        buttonRegister.setOnClickListener {
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            register(username, email, password)
        }
        buttonRegister.isEnabled = false;

        val buttonAlreadyAccount = binding.alreadyAccount
        buttonAlreadyAccount.setOnClickListener {
            alreadyRegistered()
        }


        val errorText = binding.errorPassword


        val textWatcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val username = binding.username.text.toString()
                val email = binding.email.text.toString()
                val password = binding.password.text.toString()
                val confirmPassword = binding.confirmPassword.text.toString()

                if (username.isEmpty() || email.isEmpty()) {
                    buttonRegister.isEnabled = false
                } else if (!validateEmail(email)) {
                    buttonRegister.isEnabled = false
                }
                if (!validatePassword(password)) {
                    errorText.error = "La contraseña debe tener 1 mayúscula, 1 número y 8 caracteres."
                    buttonRegister.isEnabled = false
                }  else if (password != confirmPassword) {
                    errorText.error = "Las contraseñas no coinciden"
                    buttonRegister.isEnabled = false
                }
                else {
                    errorText.error = null
                    buttonRegister.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        binding.username.addTextChangedListener(textWatcher)
        binding.email.addTextChangedListener(textWatcher)
        binding.password.addTextChangedListener(textWatcher)
        binding.confirmPassword.addTextChangedListener(textWatcher)

    }


    fun alreadyRegistered() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    fun register(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val firebaseUser = auth.currentUser

                    val user = hashMapOf(
                        "username" to username
                    )
                    if (firebaseUser != null) {
                        db.collection("users")
                            .document(firebaseUser.uid) // Usamos la UID como ID del documento
                            .set(user)
                            .addOnSuccessListener { documentReference ->
                                Log.d(TAG, "DocumentSnapshot added with ID: ${firebaseUser.uid}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }

    }

    fun validatePassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return regex.matches(password)
    }

    fun validateEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return regex.matches(email)
    }
}