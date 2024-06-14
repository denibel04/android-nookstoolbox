package com.example.animalcrossing.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main activity that serves as the entry point of the application.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    var auth = FirebaseAuth.getInstance()

    @Inject
    lateinit var userRepository: UserRepository

    /**
     * Called when the activity is first created.
     * Initializes the navigation components and sets up the toolbar.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.villagerListFragment,
                R.id.profileFragment
            )
        )
        navView.setupWithNavController(navController)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        auth.currentUser?.let {
            setUsername()
        }

        binding.profile.text

        binding.profile.setOnClickListener {
            navController.popBackStack()
            navController.navigate(R.id.profile)
            navView.uncheckAllItems()
        }
    }

    /**
     * Called when the activity is becoming visible to the user.
     * Checks if the current user is authenticated and navigates to the login screen if not.
     */
    public override fun onStart() {
        super.onStart()

        val db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
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
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
        }
    }

    /**
     * Sets the username in the UI by collecting data from the user repository.
     */
    private fun setUsername() {
        lifecycleScope.launch {
            userRepository.profile.collect {
                binding.profile.text = it.username
            }

        }
    }

    /**
     * Extension function to uncheck all items in the BottomNavigationView.
     */
    private fun BottomNavigationView.uncheckAllItems() {
        menu.setGroupCheckable(0, true, false)
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }
        menu.setGroupCheckable(0, true, true)
    }
}