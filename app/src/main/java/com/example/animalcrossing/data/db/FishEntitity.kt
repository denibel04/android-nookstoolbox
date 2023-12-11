package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.animalcrossing.data.repository.Fish
import com.example.animalcrossing.data.repository.Villager

@Entity(tableName = "fish")
data class FishEntity (
    @PrimaryKey
    val name:String,
    val image_url:String,
    val location:String,
    val shadow_size:String,
    val rarity:String
)

fun List<FishEntity>.asFish():List<Fish>{
    return this.map {
        Fish(it.name.replaceFirstChar { c -> c.uppercase() },
            it.image_url,
            it.location,
            it.shadow_size,
            it.rarity)
    }
}