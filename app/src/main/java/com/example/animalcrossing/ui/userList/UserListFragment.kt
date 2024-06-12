package com.example.animalcrossing.ui.userList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.animalcrossing.R
import com.example.animalcrossing.databinding.FragmentUsersListBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserListFragment : Fragment() {

    private lateinit var binding: FragmentUsersListBinding
    private val viewModel: UserListViewModel by viewModels()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.user_list)

        val adapter = UserListAdapter(requireContext(), onFollowClicked = { user ->
            if (user.followers?.contains(currentUser?.uid) == true) {
                viewModel.unfollowUser(user.uid)
            } else {
                viewModel.followUser(user.uid)
            }
        }, onUserClicked = { uid ->
            val action =
                UserListFragmentDirections.actionUserListFragmentToUserDetailFragment(
                    uid
                )
            findNavController().navigate(action)
        })


        val rv = binding.users
        rv.adapter = adapter

        binding.searchView.getEditText().setOnEditorActionListener { _, _, _ ->
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

