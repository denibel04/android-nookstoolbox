package com.example.animalcrossing.ui.userDetail

import com.example.animalcrossing.data.repository.Villager

data class UserDetailUiState (
        val uid:String = "",
        val email:String = "",
        var username:String = "",
        var profile_picture:String = "",
        var dreamCode: String? = null,
        val followers: List<String>? = null,
        val following: List<String>? = null,
        val islandName: String = "",
        val hemisphere: String = "",
        val islandExists: Boolean = false,
        val villagers: List<Villager?> = List(10) { null }
)