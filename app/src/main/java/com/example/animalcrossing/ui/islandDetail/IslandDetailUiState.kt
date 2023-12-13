package com.example.animalcrossing.ui.islandDetail


data class IslandDetailUiState (
    val islandId:Long?=null,
    val name:String="",
    val islandExists:Boolean = false,
    val errorMessage:String?=null,
)