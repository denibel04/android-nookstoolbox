package com.example.animalcrossing.ui.userList

import com.example.animalcrossing.data.firebase.UserDetail

data class UserListUiState(
    val users: List<UserDetail> = emptyList()
)
