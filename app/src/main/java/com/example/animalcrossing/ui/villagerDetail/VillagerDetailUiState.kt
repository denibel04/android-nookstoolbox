package com.example.animalcrossing.ui.villagerDetail

import com.example.animalcrossing.data.repository.Villager

class VillagerDetailUiState (
        val name:String = "",
        val species:String = "",
        val personality:String = "",
        val image_url:String = "",
        val gender:String = "",
        val birthday_month: String = "",
        val birthday_day: Int = 0
)