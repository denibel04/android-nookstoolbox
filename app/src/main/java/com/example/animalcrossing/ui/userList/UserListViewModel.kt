package com.example.animalcrossing.ui.userList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    init {

        viewModelScope.launch {
                val users = repository.getUsers()
                _uiState.value = _uiState.value.copy(users = users)
        }

    }

    fun followUser(uid: String) {
        viewModelScope.launch {
            updateUserState(uid, follow = true)
            repository.followUser(uid)
        }
    }

    fun unfollowUser(uid: String) {
        viewModelScope.launch {
            updateUserState(uid, follow = false)
            repository.unfollowUser(uid)
        }
    }

    fun getFilteredUsers(search: String) {
        viewModelScope.launch {
            val users = repository.getFilteredUsers(search)
            _uiState.value = _uiState.value.copy(users = users)
        }
    }

    private fun updateUserState(uid: String, follow: Boolean) {
        val currentState = _uiState.value
        val currentUserUid = currentUser?.uid

        if (currentUserUid != null) {
            val updatedUsers = currentState.users.map { user ->
                if (user.uid == uid) {
                    val updatedFollowers = if (follow) {
                        user.followers?.toMutableList()?.apply { add(currentUserUid) } ?: listOf(currentUserUid)
                    } else {
                        user.followers?.filter { it != currentUserUid }
                    }
                    val updatedUser = user.copy(followers = updatedFollowers)
                    updatedUser
                } else {
                    user
                }
            }
            _uiState.value = currentState.copy(users = updatedUsers)
        }
    }
}