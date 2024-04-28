package com.example.animalcrossing.data.api

import com.example.animalcrossing.data.db.VillagerEntity
import java.time.Month

class AcnhApiModel {}


data class VillagerDetail (
    val name:String,
    val species:String,
    val personality:String,
    val image_url:String,
    val gender:String,
    val birthday_month: String,
    val birthday_day: Int
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

