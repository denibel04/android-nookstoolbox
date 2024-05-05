package com.example.animalcrossing.ui.loansDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.IslandRepository
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoansDetailViewModel @Inject constructor(private val repository: LoanRepository) :
    ViewModel() {

    fun addLoan(title: String, type: String, amountPaid: Int, amountTotal: Int) {
        viewModelScope.launch {
            repository.addLoan(title, type, amountPaid, amountTotal, false)
        }
    }

}