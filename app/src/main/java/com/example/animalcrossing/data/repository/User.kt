package com.example.animalcrossing.data.repository

data class User (
    val uid:String,
    val email:String,
    val username:String,
    val profile_picture:String,
    val dreamCode: String? = null,
    val followers: Int? = null,
    val following: Int? = null
) {
    constructor() : this("", "", "", "")
}
