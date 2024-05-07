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
    suspend fun getLoan(loanId: Long): Flow<LoansEntity> {
        return acnhDao.getLoan(loanId)
    }

    @WorkerThread
    suspend fun deleteLoan (loanId: Long) {
        val loan = LoansEntity(loanId = loanId, title = "", type = "", amountPaid = 0, amountTotal = 0, completed = true)
        acnhDao.deleteLoan(loan)
    }

    @WorkerThread
    suspend fun updateLoan (loan: Loan) {
        val loan = LoansEntity(loanId = loan.loanId, title = loan.title, type = loan.type, amountPaid = loan.amountPaid, amountTotal = loan.amountTotal, completed = loan.completed)
        acnhDao.updateLoan(loan)
    }
}