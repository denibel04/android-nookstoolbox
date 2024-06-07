package com.example.animalcrossing.ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.User
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.FragmentProfileBinding
import com.example.animalcrossing.databinding.GeneralDialogBinding
import com.example.animalcrossing.ui.LoginActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    var auth = FirebaseAuth.getInstance()


    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = "My Profile"


        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this.requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }


        val adapter = ProfileUsersAdapter(requireContext())
        val rv = binding.friendList
        rv.adapter = adapter

        setupTabs(adapter)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                adapter.submitList(uiState.friends)
            }
        }



        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                uiState.currentUser.collect{user ->
                    if (user != null) {
                        binding.username.setOnClickListener {
                            showDialog("username", user).show()
                        }

                        binding.dreamCode.setOnClickListener{
                            showDialog("dreamcode", user).show()
                        }
                        binding.profilePicture.setOnClickListener {
                            val modalBottomSheet = PictureOptionsFragment(user, userRepository)
                            modalBottomSheet.show(
                                requireActivity().supportFragmentManager,
                                PictureOptionsFragment.TAG
                            )
                        }
                    }



                    if (user?.profile_picture?.isNotEmpty() == true) {
                        Glide.with(requireContext())
                            .load(user.profile_picture)
                            .into(binding.profilePicture)
                    } else {
                        Glide.with(requireContext()).clear(binding.profilePicture)
                    }
                    var username = "@"+user?.username
                    if (username.length > 10) {
                        binding.username.textSize = 23F
                    }
                    if (username.length > 15) {
                        binding.username.textSize = 20F
                    }
                    binding.username.text = username
                    binding.followedTextView.text = "Siguiendo: ${user?.following ?: 0}"
                    binding.followersTextView.text = "Seguidores: ${user?.followers ?: 0}"
                    if (user?.dreamCode != "" && user?.dreamCode != null) {
                        binding.dreamCode.text = user.dreamCode
                        binding.dreamCode.textSize = 15F
                    } else {
                        binding.dreamCode.text = "Haga click para asignar un código"
                    }

                }
                }


            }

        }

    private fun showDialog(type: String, user: User): Dialog {
        val builder = AlertDialog.Builder(activity)
        val dialogBinding: GeneralDialogBinding = GeneralDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogView = dialogBinding.root


        when (type) {
            "username" -> {
                dialogBinding.generalView.text = "Introduzca el nuevo nombre de usuario"
                dialogBinding.generalEdit.setText(user.username)
                dialogBinding.generalEdit.visibility = View.VISIBLE
                dialogBinding.maskedEdit.visibility = View.GONE
            }
            "dreamcode" -> {
                dialogBinding.generalView.text = "Introduzca el nuevo código de ensueño"
                dialogBinding.generalEdit.visibility = View.GONE
                dialogBinding.maskedEdit.visibility = View.VISIBLE

                dialogBinding.maskedEdit.addTextChangedListener(object : TextWatcher {
                    var isUpdating: Boolean = false
                    val mask = "DA-####-####-####"

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        if (isUpdating) {
                            return
                        }

                        isUpdating = true

                        val digitsOnly = s.toString().filter { it.isDigit() }
                        val maskedText = StringBuilder()

                        var digitIndex = 0
                        var maskIndex = 0

                        while (maskIndex < mask.length && digitIndex < digitsOnly.length) {
                            val maskChar = mask[maskIndex]
                            if (maskChar == '#') {
                                maskedText.append(digitsOnly[digitIndex])
                                digitIndex++
                            } else {
                                maskedText.append(maskChar)
                                if (s != null && s.length > maskedText.length && s[maskedText.length] == maskChar) {
                                    maskedText.append(maskChar)
                                    maskIndex++
                                }
                            }
                            maskIndex++
                        }

                        dialogBinding.maskedEdit.setText(maskedText)
                        dialogBinding.maskedEdit.setSelection(maskedText.length)

                        isUpdating = false
                    }
                })

            }
        }

        builder.setView(dialogView)
            .setPositiveButton("okay") { dialog, id ->
                val editText = if (type == "dreamcode") {
                    dialogBinding.maskedEdit.text.toString()
                } else {
                    dialogBinding.generalEdit.text.toString()
                }

                viewLifecycleOwner.lifecycleScope.launch {
                    when (type) {
                        "username" -> {
                            user.username = editText
                            viewModel.changeUsername(user)
                        }
                        "dreamcode" -> {
                            user.dreamCode = editText
                            Log.d("dreamcode", editText)
                            viewModel.changeDreamCode(user)
                        }
                    }

                }
            }
            .setNegativeButton("no") { dialog, id ->
                dialog.cancel()
            }
        return builder.create()
    }

    private fun setupTabs(adapter: ProfileUsersAdapter) {
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Amigos"))
        tabLayout.addTab(tabLayout.newTab().setText("Seguidores"))
        tabLayout.addTab(tabLayout.newTab().setText("Siguiendo"))


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewLifecycleOwner.lifecycleScope.launch {
                    when (tab.position) {
                        0 -> {
                            viewModel.uiState.collect { uiState ->
                                adapter.submitList(uiState.friends)
                            }
                        }

                        1 -> {
                            viewModel.uiState.collect { uiState ->
                                adapter.submitList(uiState.followers)
                            }
                        }

                        2 -> {
                            viewModel.uiState.collect { uiState ->
                                adapter.submitList(uiState.following)
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

}