package com.example.animalcrossing.ui.profile

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.FragmentProfileBinding
import com.example.animalcrossing.databinding.GeneralDialogBinding
import com.example.animalcrossing.ui.LoginActivity
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
        binding.profilePicture.setOnClickListener {
            val modalBottomSheet = PictureOptionsFragment()
            modalBottomSheet.show(
                requireActivity().supportFragmentManager,
                PictureOptionsFragment.TAG
            )
        }

        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this.requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }

        binding.username.setOnClickListener {
            showDialog("username").show()
        }

        binding.dreamCode.setOnClickListener{
            showDialog("dreamcode").show()
        }

        val adapter = ProfileUsersAdapter(requireContext())
        val rv = binding.friendList
        rv.adapter = adapter

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                val user = uiState.currentUser
                    Log.d("PROFILE PICTURE", user.toString())
                    if (user?.profile_picture?.isNotEmpty() == true) {
                        Glide.with(requireContext())
                            .load(user.profile_picture)
                            .into(binding.profilePicture)
                    } else {
                        Glide.with(requireContext()).clear(binding.profilePicture)
                    }
                    binding.username.text = user?.username
                binding.followedTextView.text = "Siguiendo: ${user?.following?.size ?: 0}"
                binding.followersTextView.text = "Seguidores: ${user?.followers?.size ?: 0}"
                    if (user?.dreamCode != null) {
                        binding.dreamCode.text = user.dreamCode
                    }

                }

            }

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                Log.d("ENTRA", uiState.friends.toString())
                adapter.submitList(uiState.friends)
                }
            }
        }

    private fun showDialog(type: String): Dialog {
        val builder = AlertDialog.Builder(activity)
        val dialogBinding: GeneralDialogBinding = GeneralDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val dialogView = dialogBinding.root


        when (type) {
            "username" -> {
                dialogBinding.generalView.text = "Introduzca el nuevo nombre de usuario"
                dialogBinding.generalEdit.hint = "Nombre de usuario"
            }
            "dreamcode" -> {
                dialogBinding.generalView.text = "Introduzca el nuevo código de ensueño"
                dialogBinding.generalEdit.hint = "Código de ensueño"
            }
        }

        builder.setView(dialogView)
            .setPositiveButton("okay") { dialog, id ->
                val editText = dialogBinding.generalEdit.text.toString()

                viewLifecycleOwner.lifecycleScope.launch {
                    when (type) {
                        "username" -> {
                            viewModel.changeUsername(editText)
                        }
                        "dreamcode" -> {
                            viewModel.changeDreamCode(editText)
                        }
                    }

                }
            }
            .setNegativeButton("no") { dialog, id ->
                dialog.cancel()
            }
        return builder.create()
    }

}