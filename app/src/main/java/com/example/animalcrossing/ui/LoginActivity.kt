package com.example.animalcrossing.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity that handles user login.
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding

    /**
     * Called when the activity is first created.
     * Initializes the UI elements and sets up event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
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

    /**
     * Attempts to log the user in with the provided email and password.
     *
     * @param email The email address entered by the user.
     * @param password The password entered by the user.
     */
    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val db = FirebaseFirestore.getInstance()

                    user?.let {
                        db.collection("users").document(user.uid).get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val role = document.getString("role")
                                    if (role == "banned") {
                                        Toast.makeText(
                                            baseContext,
                                            getString(R.string.banned_toast),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        auth.signOut()
                                    } else {
                                        Toast.makeText(
                                            baseContext,
                                            getString(R.string.login_success),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        val intent = Intent(this, SplashActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    Toast.makeText(
                                        baseContext,
                                        getString(R.string.login_fail),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                    }
                }
            } .addOnFailureListener(this) { e ->
                Toast.makeText(baseContext, getString(R.string.login_fail), Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Redirects the user to the account registration activity.
     */
    private fun newAccount() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }
}