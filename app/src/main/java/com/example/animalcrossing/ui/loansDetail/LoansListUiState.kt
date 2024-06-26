package com.example.animalcrossing.ui.loansDetail

import com.example.animalcrossing.data.repository.Loan

data class LoansListUiState(
    val loans: List<Loan>,
    val completedLoans: List<Loan>,
    val islandExists: Boolean,
    val errorMessage: String? = null
)