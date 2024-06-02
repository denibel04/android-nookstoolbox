package com.example.animalcrossing.ui.loansDetail

import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.Villager

data class LoansListUiState(
    val loans: List<Loan>,
    val completedLoans: List<Loan>,
    val islandExists: Boolean,
    val errorMessage: String? = null
)