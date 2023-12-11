package com.example.animalcrossing.ui.fishList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject


@HiltViewModel
class FishListViewModel @Inject constructor(private val repository: FishRepository):
    ViewModel(){
    private val _uiState = MutableStateFlow(FishListUiState(listOf()))
    val uiState: StateFlow<FishListUiState>
        get() = _uiState.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                repository.refreshList()
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(errorMessage=e.message!!)
            }
        }


        viewModelScope.launch {
            repository.fish.collect {
                _uiState.value = FishListUiState(it)
            }
        }
    }
}