package com.example.animalcrossing.data.db

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class IslandWithLoans(
    @Embedded val island: IslandEntity,

    @Relation(
        parentColumn = "islandId",
        entityColumn = "loanId",
        associateBy = Junction(IslandLoansCrossRef::class)
    )
    val loans: List<LoansEntity>
)