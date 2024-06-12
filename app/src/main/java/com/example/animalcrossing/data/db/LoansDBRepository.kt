package com.example.animalcrossing.data.db

import androidx.annotation.WorkerThread
import com.example.animalcrossing.data.repository.Loan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoansDBRepository  @Inject constructor(private val acnhDao: AcnhDao) {

    val allLoans: Flow<List<LoansEntity>> = acnhDao.getAllLoans()

    @WorkerThread
    suspend fun insert (loansEntity: LoansEntity): Long {
        return acnhDao.insertLoan(loansEntity)
    }

    @WorkerThread
    suspend fun getLoan(loanId: String): Flow<LoansEntity> {
        return acnhDao.getLoan(loanId)
    }

    @WorkerThread
    suspend fun deleteLoan (firebaseId: String) {
        val loan = LoansEntity(firebaseId = firebaseId, title = "", type = "", amountPaid = 0, amountTotal = 0, completed = true)
        acnhDao.deleteLoan(loan)
    }

    @WorkerThread
    suspend fun updateLoan (loan: Loan) {
        val newLoan = LoansEntity(firebaseId = loan.firebaseId, title = loan.title, type = loan.type, amountPaid = loan.amountPaid, amountTotal = loan.amountTotal, completed = loan.completed)
        acnhDao.updateLoan(newLoan)
    }
}