package com.example.animalcrossing.ui.fishList

import com.example.animalcrossing.data.repository.Fish

data class FishListUiState (
    val fish:List<Fish>,
    val errorMessage:String?=null
)