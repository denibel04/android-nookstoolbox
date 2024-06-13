package com.example.animalcrossing.data.firebase

import com.example.animalcrossing.data.db.VillagerEntity

data class VillagerDetail (
    val name:String,
    val species:String,
    val personality:String,
    val image_url:String,
    val gender:String,
    val birthday_month: String,
    val birthday_day: Int
)

data class IslandDetail (
    val islandId:String,
    val name:String,
    val hemisphere: String,
    val villagers: List<String>
)

data class UserDetail (
    val uid:String,
    val email:String,
    val username:String,
    val profile_picture:String,
    val dreamCode: String? = null,
    val role: String? = null,
    val followers: List<String>? = null,
    val following: List<String>? = null
)


fun List<VillagerDetail>.asEntityModel():List<VillagerEntity> {
    return this.map {
        VillagerEntity(
            it.name,
            it.species,
            it.personality,
            it.image_url,
            it.gender,
            it.birthday_month,
            it.birthday_day
        )
    }
}

