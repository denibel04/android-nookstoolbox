package com.example.animalcrossing.ui.list

import com.example.animalcrossing.data.repository.Villager

data class VillagerListUiState(
    val villager: List<Villager>,
    val errorMessage: String? = null
)