package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "island_villager_cross_ref",
    primaryKeys = ["islandId", "name"],
    foreignKeys = [
        ForeignKey(
            entity = IslandEntity::class,
            parentColumns = ["islandId"],
            childColumns = ["islandId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VillagerEntity::class,
            parentColumns = ["name"],
            childColumns = ["name"],
        )
    ]
)
data class IslandVillagerCrossRef(
    val islandId: Long,
    val name: String
)