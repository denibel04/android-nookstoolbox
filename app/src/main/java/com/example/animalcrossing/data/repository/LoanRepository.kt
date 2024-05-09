package com.example.animalcrossing.data.repository

import android.util.Log
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.db.LoansDBRepository
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.db.asLoan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val dbRepository: LoansDBRepository,
    private val apiRepository: AcnhFirebaseRepository
) {

    val loans: Flow<List<Loan>>
        get() {
            val list = dbRepository.allLoans.map { it.asLoan() }

            return list
        }

    suspend fun addLoan(title: String, type: String, amountPaid: Int, amountTotal: Int, completed: Boolean): Long {
        val newLoan = LoansEntity(firebaseId = "", title = title, type = type, amountPaid = amountPaid, amountTotal = amountTotal, completed = completed)
        val firebaseId = apiRepository.createLoan(newLoan)
        newLoan.firebaseId = firebaseId
        return dbRepository.insert(newLoan)
    }

    suspend fun getLoan(firebaseId: String): Flow<LoansEntity> {
        return dbRepository.getLoan(firebaseId)
    }

    suspend fun deleteLoan(firebaseId: String) {
        apiRepository.deleteLoan(firebaseId)
        dbRepository.deleteLoan(firebaseId)
    }

    suspend fun updateLoan(loan: Loan) {
        apiRepository.editLoan(loan);
        dbRepository.updateLoan(loan)
    }
}