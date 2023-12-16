package com.example.animalcrossing.ui.islandDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.db.IslandVillagerCrossRef
import com.example.animalcrossing.data.db.asVillager
import com.example.animalcrossing.data.repository.IslandRepository
import com.example.animalcrossing.data.repository.Villager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IslandDetailViewModel @Inject constructor(private val repository: IslandRepository) :
    ViewModel() {
    private val _uiState = MutableStateFlow(IslandDetailUiState())
    val uiState: StateFlow<IslandDetailUiState>
        get() = _uiState.asStateFlow()

    private val _villagers = MutableStateFlow<List<Villager?>>(List(10) { null })
    val villagers: StateFlow<List<Villager?>> = _villagers.asStateFlow()


    init {
        fetchIsland()
    }

    private fun fetchIsland() {
        viewModelScope.launch {
            repository.islandWithVillagers.collect { islandWithVillagers ->

                _uiState.value = if (islandWithVillagers != null) {
                    val currentList = _villagers.value.toMutableList()
                    islandWithVillagers!!.villagers.asVillager().forEachIndexed { index, villager ->
                        if (index < 10) {
                            currentList[index] = villager
                        }
                    }
                    _villagers.value = currentList

                    Log.d("IWV", islandWithVillagers.toString())
                    IslandDetailUiState(
                        islandId = islandWithVillagers.island.islandId,
                        name = islandWithVillagers.island.name,
                        islandExists = true,
                        villagers = islandWithVillagers.villagers.asVillager()
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
            uiState.value.islandId?.let { repository.deleteIsland(it) }
            _uiState.value = IslandDetailUiState(islandExists = false)
        }
    }

    fun renameIsland(newName: String) {
        viewModelScope.launch {
            val currentIslandId = uiState.value.islandId
            if (currentIslandId != null) {
                repository.renameIsland(currentIslandId, newName)
                fetchIsland()
            }
        }
    }

    suspend fun searchVillagers(query: String): Flow<List<Villager>> {
        return repository.searchVillagers(query).map { it.asVillager() }
    }

    fun addVillagerToIsland(name: String, islandId: Long) {
        viewModelScope.launch {
            repository.addVillagerToIsland(name, islandId)
            fetchIsland()
        }
    }
}
