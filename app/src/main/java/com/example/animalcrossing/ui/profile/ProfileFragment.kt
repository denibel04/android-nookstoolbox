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
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.bumptech.glide.Glide
import com.example.animalcrossing.R
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.FragmentProfileBinding
import com.example.animalcrossing.databinding.FragmentVillagerListBinding
import com.example.animalcrossing.databinding.GeneralDialogBinding
import com.example.animalcrossing.databinding.LoanDialogBinding
import com.example.animalcrossing.ui.LoginActivity
import com.example.animalcrossing.ui.islandDetail.IslandDetailAdapter
import com.example.animalcrossing.ui.list.VillagerListAdapter
import com.example.animalcrossing.ui.list.VillagerListFragmentDirections
import com.example.animalcrossing.ui.list.VillagerListViewModel
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

        val adapter = ProfileUsersAdapter(requireContext(), onFollowClicked = { user ->
        })
        val rv = binding.randomUsers
        rv.adapter = adapter

        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                uiState.let { user ->
                    Log.d("PROFILE PICTURE", user.toString())
                    if (user.currentUser?.profile_picture?.isNotEmpty() == true) {
                        Glide.with(requireContext())
                            .load(user.currentUser.profile_picture)
                            .into(binding.profilePicture)
                    } else {
                        Glide.with(requireContext()).clear(binding.profilePicture)
                    }
                    binding.username.text = user.currentUser?.username
                    binding.followedTextView.text = "Siguiendo: "+user.currentUser?.following?.size
                    binding.followersTextView.text = "Seguidores: "+user.currentUser?.followers?.size
                    if (user.currentUser?.dreamCode != null) {
                        binding.dreamCode.text = user.currentUser.dreamCode
                    }

                }
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