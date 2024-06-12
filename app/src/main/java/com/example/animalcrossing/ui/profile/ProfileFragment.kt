package com.example.animalcrossing.ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
    val currentUser = auth.currentUser


    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.my_profile)


        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this.requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }


        val adapter = ProfileUsersAdapter(requireContext(), onFollowClicked = { user ->
            if (user.followers?.contains(currentUser?.uid) == true) {
                viewModel.unfollowUser(user.uid)
            } else {
                viewModel.followUser(user.uid)
            }}, onUserClicked = { uid ->
            val action =
                ProfileFragmentDirections.actionProfileFragmentToUserDetailFragment(
                    uid
                )
            findNavController().navigate(action)
        })

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
                    val username = "@"+user?.username
                    if (username.length > 10) {
                        binding.username.textSize = 23F
                    }
                    if (username.length > 15) {
                        binding.username.textSize = 20F
                    }
                    binding.username.text = username
                    binding.followedTextView.text = getString(R.string.following_count, user?.following ?: 0)
                    binding.followersTextView.text = getString(R.string.followers_count, user?.followers ?: 0)
                    if (user?.dreamCode != "" && user?.dreamCode != null) {
                        binding.dreamCode.text = user.dreamCode
                        binding.dreamCode.textSize = 15F
                    } else {
                        binding.dreamCode.text = getString(R.string.no_dream_code)
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
                dialogBinding.generalView.text = getString(R.string.set_username)
                dialogBinding.generalEdit.setText(user.username)
                dialogBinding.generalEdit.visibility = View.VISIBLE
                dialogBinding.maskedEdit.visibility = View.GONE
            }
            "dreamcode" -> {
                dialogBinding.generalView.text = getString(R.string.set_dream_code)
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
            .setPositiveButton(getString(R.string.accept)) { _, _ ->
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
                            viewModel.changeDreamCode(user)
                        }
                    }

                }
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
        return builder.create()
    }

    private fun setupTabs(adapter: ProfileUsersAdapter) {
        val tabLayout = binding.tabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_friends)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_followers)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.tab_following)))


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setTab(tab.position)
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