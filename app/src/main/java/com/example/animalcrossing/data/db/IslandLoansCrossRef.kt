package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "island_loans_cross_ref",
    primaryKeys = ["islandId", "loanId"],
    foreignKeys = [
        ForeignKey(
            entity = IslandEntity::class,
            parentColumns = ["islandId"],
            childColumns = ["islandId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LoansEntity::class,
            parentColumns = ["loanId"],
            childColumns = ["loanId"],
        )
    ]
)
data class IslandLoansCrossRef(
    val islandId: Long,
    val loanId: Long
)