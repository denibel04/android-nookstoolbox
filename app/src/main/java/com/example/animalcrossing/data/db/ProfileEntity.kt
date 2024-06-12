package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.animalcrossing.data.repository.User

@Entity(tableName = "profile")
data class ProfileEntity (
    @PrimaryKey
    val uid:String,
    val email:String,
    val username:String,
    val profile_picture: String,
    val dreamCode: String,
    val followers: Int,
    val following: Int
)

fun ProfileEntity?.asUser(): User? {
    return this?.let {
        User(
            it.uid,
            "",
            it.username,
            it.profile_picture,
            it.dreamCode,
            it.followers,
            it.following
        )
    }
}