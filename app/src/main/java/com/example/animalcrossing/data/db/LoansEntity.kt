package com.example.animalcrossing.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.animalcrossing.data.repository.Island
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.Villager

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

fun List<LoansEntity>.asLoan():List<Loan>{
    return this.map {
        Loan(it.loanId,
            it.title.replaceFirstChar { c -> c.uppercase() },
            it.type,
            it.amountPaid,
            it.amountTotal,
            it.completed)
    }
}



