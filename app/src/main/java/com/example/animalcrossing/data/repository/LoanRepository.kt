package com.example.animalcrossing.data.repository

import com.example.animalcrossing.data.api.AcnhApiRepository
import com.example.animalcrossing.data.api.asEntityModel
import com.example.animalcrossing.data.db.IslandEntity
import com.example.animalcrossing.data.db.LoansDBRepository
import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.db.VillagerDBRepository
import com.example.animalcrossing.data.db.VillagerEntity
import com.example.animalcrossing.data.db.asVillager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val dbRepository: LoansDBRepository,
    private val apiRepository: AcnhApiRepository
) {

    suspend fun addLoan(title: String, type: String, amountPaid: Int, amountTotal: Int, completed: Boolean) {
        val newLoan = LoansEntity(title = title, type = type, amountPaid = amountPaid, amountTotal = amountTotal, completed = completed)
        dbRepository.insert(newLoan)
    }

    suspend fun getLoan(loanId: Long): Flow<LoansEntity> {
        return dbRepository.getLoan(loanId)
    }
    suspend fun deleteLoan(loanId:Long) {
        dbRepository.deleteLoan(loanId)
    }
}