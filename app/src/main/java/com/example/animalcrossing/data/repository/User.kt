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
