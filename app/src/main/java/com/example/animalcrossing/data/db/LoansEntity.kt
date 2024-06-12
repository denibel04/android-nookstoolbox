package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.animalcrossing.data.repository.Loan

@Entity(tableName = "loans")
data class LoansEntity (
    @PrimaryKey
    var firebaseId: String,
    val title:String,
    val type: String,
    val amountPaid: Int,
    val amountTotal: Int,
    val completed: Boolean

)

fun List<LoansEntity>.asLoan():List<Loan>{
    return this.map {
        Loan(it.firebaseId,
            it.title,
            it.type,
            it.amountPaid,
            it.amountTotal,
            it.completed)
    }
}



