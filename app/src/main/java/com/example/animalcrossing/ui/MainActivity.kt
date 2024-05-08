package com.example.animalcrossing.ui

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    var auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView:BottomNavigationView = binding.navView
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
            getUserDetails(it.uid) {
                binding.profile.text = it
            }
        }

        binding.profile.text

        binding.profile.setOnClickListener {
            /*auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)*/
            navController.popBackStack()
            navController.navigate(R.id.profile)
            navView.uncheckAllItems()

        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    fun getUserDetails(uid: String, onUserDetails: (String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val username = userData?.get("username") as String
                    onUserDetails(username)
                } else {
                    Log.d(TAG, "Usuario con UID $uid no encontrado")
                    onUserDetails(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error obteniendo detalles del usuario con UID $uid", e)
                onUserDetails(null)
            }
    }

    fun BottomNavigationView.uncheckAllItems() {
        menu.setGroupCheckable(0, true, false)
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }
        menu.setGroupCheckable(0, true, true)
    }

}
