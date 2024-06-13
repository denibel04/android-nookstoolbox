package com.example.animalcrossing.ui.islandDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.db.asVillager
import com.example.animalcrossing.data.repository.IslandRepository
import com.example.animalcrossing.data.repository.Villager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the state and operations related to the island detail screen.
 *
 * @property repository The repository providing access to island-related data.
 */
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

    /**
     * Fetches the island details and updates the UI state accordingly.
     */
    private fun fetchIsland() {
        viewModelScope.launch {
            repository.islandWithVillagers.collect { islandWithVillagers ->
                _uiState.value = if (islandWithVillagers != null) {
                    val currentList = _villagers.value.toMutableList()
                    islandWithVillagers.villagers.asVillager().forEachIndexed { index, villager ->
                        if (index < 10) {
                            currentList[index] = villager
                        }
                    }
                    _villagers.value = currentList

                    IslandDetailUiState(
                        islandId = islandWithVillagers.island.islandId,
                        name = islandWithVillagers.island.name,
                        hemisphere = islandWithVillagers.island.hemisphere,
                        islandExists = true,
                        villagers = islandWithVillagers.villagers.asVillager()
                    )
                } else {
                    IslandDetailUiState(islandExists = false)
                }
            }
        }
    }

    /**
     * Creates a new island with the provided name and hemisphere.
     *
     * @param islandName The name of the island to create.
     * @param hemisphere The hemisphere of the island to create.
     */
    fun createIsland(islandName: String, hemisphere: String) {
        viewModelScope.launch {
            repository.addIsland(islandName, hemisphere)
            fetchIsland()
        }
    }

    /**
     * Deletes the current island.
     */
    fun deleteIsland() {
        viewModelScope.launch {
            uiState.value.islandId?.let { repository.deleteIsland(it) }
            _uiState.value = IslandDetailUiState(islandExists = false)
            _villagers.value = List(10) { null }
        }
    }

    /**
     * Renames the current island with the provided new name.
     *
     * @param newName The new name for the island.
     */
    fun renameIsland(newName: String) {
        viewModelScope.launch {
            val currentIslandId = uiState.value.islandId
            if (currentIslandId != null) {
                repository.renameIsland(currentIslandId, newName)
                fetchIsland()
            }
        }
    }

    /**
     * Searches for villagers based on the provided query.
     *
     * @param query The search query.
     * @return Flow emitting the list of villagers matching the query.
     */
    fun searchVillagers(query: String): Flow<List<Villager>> {
        return repository.searchVillagers(query).map { it.asVillager() }
    }

    /**
     * Adds a villager to the current island.
     *
     * @param name The name of the villager to add.
     * @param islandId The ID of the island to which the villager will be added.
     * @param index The position in the list where the villager will be added.
     */
    fun addVillagerToIsland(name: String, islandId: Long, index: Int) {
        viewModelScope.launch {
            if (isVillagerAtPosition(index)) {
                deleteVillagerFromIsland(_villagers.value.getOrNull(index)!!.name, islandId)
            }
            repository.addVillagerToIsland(name, islandId)
        }
    }

    /**
     * Checks if a villager is already present in the current island.
     *
     * @param villagerName The name of the villager to check.
     * @return `true` if the villager is already in the island, `false` otherwise.
     */
    fun isVillagerAlreadyInIsland(villagerName: String): Boolean {
        return _villagers.value.any { it?.name == villagerName }
    }

    /**
     * Deletes a villager from the current island.
     *
     * @param name The name of the villager to delete.
     * @param islandId The ID of the island from which the villager will be deleted.
     */
    fun deleteVillagerFromIsland(name: String, islandId: Long) {
        viewModelScope.launch {
            repository.deleteVillagerFromIsland(name, islandId)
            val updatedList = _villagers.value.toMutableList()
            updatedList.removeAll { it?.name == name }
            updatedList.add(9, null)
            _villagers.value = updatedList
        }
    }

    /**
     * Checks if there is a villager at the specified position in the list.
     *
     * @param index The position to check.
     * @return `true` if there is a villager at the specified position, `false` otherwise.
     */
    private fun isVillagerAtPosition(index: Int): Boolean {
        return _villagers.value.getOrNull(index) != null
    }

    /**
     * Retrieves the name of the current island.
     *
     * @return The name of the current island.
     */
    fun getIslandName(): String {
        return _uiState.value.name
    }

    /**
     * Retrieves a comma-separated string of villager names currently on the island.
     *
     * @return The comma-separated string of villager names.
     */
    fun getVillagersString(): String {
        return _uiState.value.villagers.joinToString(separator = ", ") { villager ->
            villager.name
        }
    }
}