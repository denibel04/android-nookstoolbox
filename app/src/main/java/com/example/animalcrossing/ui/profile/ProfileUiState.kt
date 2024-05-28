package com.example.animalcrossing.ui.profile

import com.example.animalcrossing.data.firebase.UserDetail
import com.example.animalcrossing.data.repository.User

data class ProfileUiState(
    val currentUser: User? = null,
    val friends: List<UserDetail> = emptyList()
)
