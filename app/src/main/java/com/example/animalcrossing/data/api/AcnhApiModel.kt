package com.example.animalcrossing.data.api

import com.example.animalcrossing.data.db.FishEntity
import com.example.animalcrossing.data.db.VillagerEntity

class AcnhApiModel {}


data class VillagerDetail (
    val name:String,
    val species:String,
    val personality:String,
    val image_url:String
)

fun List<VillagerDetail>.asEntityModel():List<VillagerEntity> {
    return this.map {
        VillagerEntity(
            it.name,
            it.species,
            it.personality,
            it.image_url
        )
    }
}

data class FishDetail (
    val name:String,
    val image_url:String,
    val location:String,
    val shadow_size:String,
    val rarity:String
)

fun List<FishDetail>.fishAsEntityModel():List<FishEntity> {
    return this.map {
        FishEntity(
            it.name,
            it.image_url,
            it.location,
            it.shadow_size,
            it.rarity
        )
    }
}