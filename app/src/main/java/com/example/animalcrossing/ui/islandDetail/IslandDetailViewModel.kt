package com.example.animalcrossing.ui.islandDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.db.IslandEntity
import com.example.animalcrossing.data.repository.IslandRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IslandDetailViewModel @Inject constructor(private val repository: IslandRepository): ViewModel(){

    private val _uiState = MutableStateFlow(IslandDetailUiState())
    val uiState: StateFlow<IslandDetailUiState>
        get() = _uiState.asStateFlow()


    init {
        fetchIsland()
    }

    fun fetchIsland() {
        viewModelScope.launch {
            repository.island.collect { island ->
                _uiState.value = if (island != null) {
                    IslandDetailUiState(
                        name = island.name,
                        islandExists = true
                    )
                } else {
                    IslandDetailUiState(islandExists = false)
                }


            }
        }
    }

    fun createIsland(islandName: String) {
        viewModelScope.launch {
            repository.addIsland(islandName)
            fetchIsland()
        }
    }

    fun deleteIsland() {
        viewModelScope.launch {
            // Supongamos que tu repositorio tiene una función para borrar por ID
            uiState.value.islandId?.let { repository.deleteIsland(it) }
            // Restablece el estado de la UI para reflejar que ya no hay isla
            _uiState.value = IslandDetailUiState(islandExists = false)
        }
    }

}