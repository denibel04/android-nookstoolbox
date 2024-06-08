package com.example.animalcrossing.ui.profile

import com.example.animalcrossing.data.firebase.UserDetail
import com.example.animalcrossing.data.repository.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class ProfileUiState(
    val currentUser: Flow<User?> = flowOf(null),
    var friends: List<UserDetail> = emptyList(),
    var followers: List<UserDetail> = emptyList(),
    var following: List<UserDetail> = emptyList(),
    val position: Int = 0
)
