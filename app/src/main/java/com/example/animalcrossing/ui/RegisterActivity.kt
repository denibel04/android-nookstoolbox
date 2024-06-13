package com.example.animalcrossing.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Activity for user registration.
 */
class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    val db = FirebaseFirestore.getInstance()

    /**
     * Called when the activity is first created.
     * Initializes the Firebase authentication and sets up the event listeners for the UI elements.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
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
        buttonRegister.isEnabled = false

        val buttonAlreadyAccount = binding.alreadyAccount
        buttonAlreadyAccount.setOnClickListener {
            alreadyRegistered()
        }

        val errorText = binding.errorPassword

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

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
                    errorText.error = getString(R.string.validate_password)
                    buttonRegister.isEnabled = false
                } else if (password != confirmPassword) {
                    errorText.error = getString(R.string.confirm_password_error)
                    buttonRegister.isEnabled = false
                } else {
                    errorText.error = null
                    buttonRegister.isEnabled = true
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        binding.username.addTextChangedListener(textWatcher)
        binding.email.addTextChangedListener(textWatcher)
        binding.password.addTextChangedListener(textWatcher)
        binding.confirmPassword.addTextChangedListener(textWatcher)
    }

    /**
     * Navigates to the login activity when the user already has an account.
     */
    private fun alreadyRegistered() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }

    /**
     * Registers a new user with the provided username, email, and password.
     *
     * @param username The username of the new user.
     * @param email The email address of the new user.
     * @param password The password for the new user.
     */
    private fun register(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser

                    val user = hashMapOf(
                        "username" to username,
                        "followers" to emptyList<String>(),
                        "following" to emptyList<String>(),
                        "role" to "normal"
                    )
                    if (firebaseUser != null) {
                        db.collection("users")
                            .document(firebaseUser.uid)
                            .set(user)
                            .addOnSuccessListener {
                                val intent = Intent(this, SplashActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                    }
                } else {
                    Toast.makeText(
                        baseContext,
                        getString(R.string.error_register),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }

    /**
     * Validates the password according to the required pattern.
     *
     * @param password The password to validate.
     * @return True if the password is valid, false otherwise.
     */
    fun validatePassword(password: String): Boolean {
        val regex = Regex("^(?=.*[A-Z])(?=.*\\d).{8,}$")
        return regex.matches(password)
    }

    /**
     * Validates the email according to the required pattern.
     *
     * @param email The email to validate.
     * @return True if the email is valid, false otherwise.
     */
    fun validateEmail(email: String): Boolean {
        val regex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return regex.matches(email)
    }
}