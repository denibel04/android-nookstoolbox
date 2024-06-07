package com.example.animalcrossing.ui

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.example.animalcrossing.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val buttonLogin = binding.login
        buttonLogin.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            login(email, password)
        }

        val buttonNewAccount = binding.newAccount
        buttonNewAccount.setOnClickListener {
            newAccount()
        }


        buttonLogin.isEnabled = false

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val email = binding.email.text.toString().trim()
                val password = binding.password.text.toString()
                buttonLogin.isEnabled = email.isNotEmpty() && password.isNotEmpty()
            }
        }

        binding.email.addTextChangedListener(textWatcher)
        binding.password.addTextChangedListener(textWatcher)
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    private fun newAccount() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }


}