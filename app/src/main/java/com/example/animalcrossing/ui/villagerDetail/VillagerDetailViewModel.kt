package com.example.animalcrossing.ui.villagerDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.VillagerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the VillagerDetailFragment, responsible for fetching and managing
 * the state of a villager's details.
 *
 * @property repository Repository to fetch villager details from.
 */
@HiltViewModel
class VillagerDetailViewModel @Inject constructor(private val repository: VillagerRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(VillagerDetailUiState())

    /**
     * StateFlow that holds the current state of the villager details.
     */
    val uiState: StateFlow<VillagerDetailUiState>
        get() = _uiState.asStateFlow()

    /**
     * Fetches the details of a villager by their name and updates the UI state.
     *
     * @param name The name of the villager to fetch details for.
     */
    fun fetch(name: String) {
        viewModelScope.launch {
            repository.getVillager(name).collect {
                _uiState.value = VillagerDetailUiState(
                    it.name,
                    it.species,
                    it.personality,
                    it.image_url,
                    it.gender,
                    it.birthday_month,
                    it.birthday_day
                )
            }
        }
    }
}