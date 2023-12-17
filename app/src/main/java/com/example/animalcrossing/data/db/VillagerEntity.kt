package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.animalcrossing.data.repository.Villager

@Entity(tableName = "villager")
data class VillagerEntity (
    @PrimaryKey
    val name:String,
    val species:String,
    val personality:String,
    val image_url:String,
    val gender:String,
    val birthday_month: String,
    val birthday_day: Int,
    val islander:Boolean=false,
)

fun List<VillagerEntity>.asVillager():List<Villager>{
    return this.map {
        Villager(it.name.replaceFirstChar { c -> c.uppercase() },
            it.species,
            it.personality,
            it.image_url,
            it.gender,
            it.birthday_month,
            it.birthday_day)
    }
}