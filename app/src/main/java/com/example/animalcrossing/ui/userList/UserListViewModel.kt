package com.example.animalcrossing.ui.userList

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

/**
 * ViewModel for managing the state of the user list in the UI.
 *
 * @property repository The UserRepository instance used for fetching and updating user data.
 */
@HiltViewModel
class UserListViewModel @Inject constructor(private val repository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    /**
     * Initializes the ViewModel and fetches the list of users.
     */
    init {
        viewModelScope.launch {
            val users = repository.getUsers()
            _uiState.value = _uiState.value.copy(users = users)
        }
    }

    /**
     * Follows a user by their UID.
     *
     * @param uid The UID of the user to follow.
     */
    fun followUser(uid: String) {
        viewModelScope.launch {
            updateUserState(uid, follow = true)
            repository.followUser(uid)
        }
    }

    /**
     * Unfollows a user by their UID.
     *
     * @param uid The UID of the user to unfollow.
     */
    fun unfollowUser(uid: String) {
        viewModelScope.launch {
            updateUserState(uid, follow = false)
            repository.unfollowUser(uid)
        }
    }

    /**
     * Retrieves a filtered list of users based on a search query.
     *
     * @param search The search query to filter users.
     */
    fun getFilteredUsers(search: String) {
        viewModelScope.launch {
            val users = repository.getFilteredUsers(search)
            _uiState.value = _uiState.value.copy(users = users)
        }
    }

    /**
     * Updates the state of the user list when a user is followed or unfollowed.
     *
     * @param uid The UID of the user to update.
     * @param follow A boolean indicating whether the user is being followed or unfollowed.
     */
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
                    user.copy(followers = updatedFollowers)
                } else {
                    user
                }
            }
            _uiState.value = currentState.copy(users = updatedUsers)
        }
    }
}