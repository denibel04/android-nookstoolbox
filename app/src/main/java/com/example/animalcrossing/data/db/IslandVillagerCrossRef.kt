package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "island_villager_cross_ref",
    primaryKeys = ["islandId", "villagerId"],
    foreignKeys = [
        ForeignKey(
            entity = IslandEntity::class,
            parentColumns = ["id"],
            childColumns = ["islandId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = VillagerEntity::class,
            parentColumns = ["name"],
            childColumns = ["villagerName"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class IslandVillagerCrossRef(
    val islandId: Int,
    val villagerId: Int
)