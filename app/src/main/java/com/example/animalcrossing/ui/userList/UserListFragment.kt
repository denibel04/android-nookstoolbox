package com.example.animalcrossing.ui.userList

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.databinding.FragmentProfileBinding
import com.example.animalcrossing.databinding.FragmentUsersListBinding
import com.example.animalcrossing.ui.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserListFragment : Fragment() {

    private lateinit var binding: FragmentUsersListBinding
    private val viewModel: UserListViewModel by viewModels()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUsersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = "User List"

        val adapter = UserListAdapter(requireContext(), onFollowClicked = { user ->

            if (user.followers?.contains(currentUser?.uid) == true) {
                viewModel.unfollowUser(user.uid)
            } else {
                viewModel.followUser(user.uid)
            }
        })
        val rv = binding.users
        rv.adapter = adapter

        binding.searchView.getEditText().setOnEditorActionListener { v, actionId, event ->
            binding.searchBar.setText(binding.searchView.getText())
            val searchQuery = binding.searchBar.text.toString()
            viewModel.getFilteredUsers(searchQuery)
            binding.searchView.hide()
            false
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                adapter.submitList(state.users)
            }
        }

    }
}
