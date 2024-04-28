package com.example.animalcrossing.ui.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.VillagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class VillagerListViewModel @Inject constructor(private val repository: VillagerRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(VillagerListUiState(listOf()))
    val uiState: StateFlow<VillagerListUiState>
        get() = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.refreshList()
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message!!)
            }
        }


        viewModelScope.launch {
            repository.villager.collect {
                _uiState.value = VillagerListUiState(it)
            }
        }
    }
}