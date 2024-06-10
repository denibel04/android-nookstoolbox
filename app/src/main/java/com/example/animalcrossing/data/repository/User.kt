package com.example.animalcrossing.data.repository

data class User (
    val uid:String,
    val email:String,
    var username:String,
    var profile_picture:String,
    var dreamCode: String? = null,
    val followers: Int? = null,
    val following: Int? = null
) {
    constructor() : this("", "", "", "")
}

data class UserProfileDetail (
    val uid:String,
    val email:String,
    var username:String,
    var profile_picture:String,
    var dreamCode: String? = null,
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val islandName: String = "",
    val hemisphere: String = "",
    val islandExists: Boolean = false,
    val villagers: List<String> = emptyList(),
)
