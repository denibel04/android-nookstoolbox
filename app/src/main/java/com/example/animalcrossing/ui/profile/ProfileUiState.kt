package com.example.animalcrossing.ui.profile

import com.example.animalcrossing.data.repository.User

data class ProfileUiState(
    val currentUser: User? = null,
    val friends: List<User> = emptyList()
)
