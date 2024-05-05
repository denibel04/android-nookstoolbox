package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.animalcrossing.data.repository.Island

@Entity(tableName = "loans")
data class LoansEntity (
    @PrimaryKey(autoGenerate = true)
    val loanId:Long = 0L,
    val title:String,
    val type: String,
    val amountPaid: Int,
    val amountTotal: Int,
    val completed: Boolean

)



