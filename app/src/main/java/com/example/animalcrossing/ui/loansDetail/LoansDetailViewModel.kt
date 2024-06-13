package com.example.animalcrossing.ui.loansDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.IslandRepository
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing loan-related data and UI logic in the LoansDetailFragment.
 *
 * @property repository The repository providing access to loan data operations.
 * @property islandRepository The repository providing access to user island data.
 */
@HiltViewModel
class LoansDetailViewModel @Inject constructor(
    private val repository: LoanRepository,
    private val islandRepository: IslandRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoansListUiState(listOf(), listOf(), false))

    /**
     * State flow representing the current UI state of loans, including incomplete and completed loans.
     */
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

    /**
     * Adds a new loan with the specified details.
     *
     * @param title The title of the loan.
     * @param type The type of the loan (e.g., bridge, stairs, house).
     * @param amountPaid The amount paid towards the loan.
     * @param amountTotal The total amount of the loan.
     * @param completed Flag indicating if the loan is completed.
     * @return The ID of the newly added loan.
     */
    suspend fun addLoan(
        title: String,
        type: String,
        amountPaid: Int,
        amountTotal: Int,
        completed: Boolean
    ): Long {
        val newLoanId = viewModelScope.async {
            repository.addLoan(title, type, amountPaid, amountTotal, completed)
        }
        return newLoanId.await()
    }

    /**
     * Updates an existing loan with new data.
     *
     * @param loan The loan object containing updated information.
     */
    suspend fun editLoan(loan: Loan) {
        repository.updateLoan(loan)
    }

    /**
     * Deletes a loan with the specified Firebase ID.
     *
     * @param firebaseId The Firebase ID of the loan to delete.
     */
    suspend fun deleteLoan(firebaseId: String) {
        repository.deleteLoan(firebaseId)
    }

}