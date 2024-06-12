package com.example.animalcrossing.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.FetchRepository
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    var auth = FirebaseAuth.getInstance()

    @Inject
    lateinit var fetchRepository: FetchRepository

    @Inject
    lateinit var userRepository: UserRepository

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
            setUsername()
        }

        binding.profile.text

        binding.profile.setOnClickListener {

            navController.popBackStack()
            navController.navigate(R.id.profile)
            navView.uncheckAllItems()

        }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                fetchRepository.onStartApp()
            }
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setUsername() {
        lifecycleScope.launch {
            userRepository.profile.collect {
                binding.profile.text = it.username
            }
        }
    }

    private fun BottomNavigationView.uncheckAllItems() {
        menu.setGroupCheckable(0, true, false)
        for (i in 0 until menu.size()) {
            menu.getItem(i).isChecked = false
        }
        menu.setGroupCheckable(0, true, true)
    }


}
