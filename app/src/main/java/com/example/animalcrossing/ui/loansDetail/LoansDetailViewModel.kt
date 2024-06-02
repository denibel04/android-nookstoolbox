package com.example.animalcrossing.ui.loansDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.IslandRepository
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.LoanRepository
import com.example.animalcrossing.ui.list.VillagerListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LoansDetailViewModel @Inject constructor(private val repository: LoanRepository, private val islandRepository: IslandRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(LoansListUiState(listOf(), listOf(), false))
    val uiState: StateFlow<LoansListUiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            islandRepository.island.collectLatest { userIsland ->
                repository.loans.collect { loans ->
                    val incompleteLoans = loans.filter { !it.completed }
                    val completedLoans = loans.filter { it.completed }
                    _uiState.value = LoansListUiState(incompleteLoans, completedLoans, userIsland != null)
                }
            }
        }
    }

    suspend fun addLoan(title: String, type: String, amountPaid: Int, amountTotal: Int, completed: Boolean):Long {
        val newLoanId = viewModelScope.async {
            repository.addLoan(title, type, amountPaid, amountTotal, completed)
        }
        return newLoanId.await()
    }

    suspend fun editLoan(loan: Loan) {
        repository.updateLoan(loan)
    }

    suspend fun deleteLoan(firebaseId: String) {
        repository.deleteLoan(firebaseId)
    }

}