package com.example.animalcrossing.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class IslandWithVillagers(
    @Embedded val island: IslandEntity,

    @Relation(
        parentColumn = "islandId",
        entityColumn = "name",
        associateBy = Junction(IslandVillagerCrossRef::class)
    )
    val villagers: List<VillagerEntity>
)