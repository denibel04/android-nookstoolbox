package com.example.animalcrossing.ui.islandDetail

import com.example.animalcrossing.data.repository.Villager


data class IslandDetailUiState(
    val islandId: Long? = null,
    val name: String = "",
    val islandExists: Boolean = false,
    val villagers: List<Villager> = emptyList(),
    val errorMessage: String? = null,
)