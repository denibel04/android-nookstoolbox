package com.example.animalcrossing.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.User
import com.example.animalcrossing.data.repository.UserRepository
import com.example.animalcrossing.data.repository.VillagerRepository
import com.example.animalcrossing.ui.list.VillagerListUiState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser


    init {

        viewModelScope.launch {
            repository.updateUser()
            repository.profile.collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = flowOf(user))
            }
        }

        viewModelScope.launch {
            val following = repository.getFollowing()
            _uiState.value = _uiState.value.copy(following = following)

            val followers = repository.getFollowers()
            _uiState.value = _uiState.value.copy(followers = followers)

            val friends = followers.intersect(following).toList()
            _uiState.value = _uiState.value.copy(friends = friends)
        }

    }

    suspend fun changeUsername(newUsername: User) {
        repository.changeUsername(newUsername)
    }

    suspend fun changeDreamCode(newDreamCode: User) {
        repository.changeDreamCode(newDreamCode)
    }

    fun setTab(position: Int) {
        viewModelScope.launch {
            when (position) {
                0 -> {
                    val friends = repository.getFriends()
                    _uiState.value = _uiState.value.copy(friends = friends, position = position)
                }
                1 -> {
                    val followers = repository.getFollowers()
                    _uiState.value = _uiState.value.copy(followers = followers, position = position)
                }
                2 -> {
                    val following = repository.getFollowing()
                    _uiState.value = _uiState.value.copy(following = following, position = position)
                }
                else -> {
                }
            }
        }
    }

    fun followUser(uid: String) {
        viewModelScope.launch {
            updateUserState(uid, follow = true)
            repository.followUser(uid)
            repository.updateUser()
        }
    }

    fun unfollowUser(uid: String) {
        viewModelScope.launch {
            updateUserState(uid, follow = false)
            repository.unfollowUser(uid)
            repository.updateUser()
        }
    }

    private suspend fun updateUserState(uid: String, follow: Boolean) {
        val currentState = _uiState.value
        val currentUserUid = currentUser?.uid

        if (currentUserUid != null) {
            // Friend List
            val updatedFriends = currentState.friends.map { user ->
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

            // Followers List
            val updatedFollowers = currentState.followers.map { user ->
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

            // Following List
            val updatedFollowing = currentState.following.map { user ->
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

            // Actualizar el estado de la UI
            _uiState.value = currentState.copy(
                friends = updatedFriends,
                followers = updatedFollowers,
                following = updatedFollowing
            )
        }
    }

}