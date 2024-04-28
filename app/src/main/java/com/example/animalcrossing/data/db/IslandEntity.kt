package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.animalcrossing.data.repository.Island

@Entity(tableName = "island")
data class IslandEntity (
    @PrimaryKey(autoGenerate = true)
    val islandId:Long = 0L,
    val name:String
)

fun IslandEntity?.asIsland(): Island? {
    return this?.let {
        Island(
            it.islandId,
            it.name.replaceFirstChar { char -> char.uppercase() }
        )
    }
}

