package com.example.animalcrossing.ui.fishDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.animalcrossing.data.repository.FishRepository
import com.example.animalcrossing.data.repository.VillagerRepository
import com.example.animalcrossing.ui.villagerDetail.VillagerDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FishDetailViewModel @Inject constructor(private val repository: FishRepository):
    ViewModel(){
    private val _uiState = MutableStateFlow(FishDetailUiState())
    val uiState: StateFlow<FishDetailUiState>
        get() = _uiState.asStateFlow()

    fun fetch(name: String) {
        viewModelScope.launch {
            repository.getFish(name).collect  {
                _uiState.value = FishDetailUiState(
                    it.name,
                    it.image_url,
                    it.location,
                    it.shadow_size,
                    it.rarity,
                )
            }
        }
    }

}