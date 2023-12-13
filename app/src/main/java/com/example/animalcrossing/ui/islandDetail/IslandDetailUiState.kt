package com.example.animalcrossing.ui.islandDetail


class IslandDetailUiState (
    val islandId:Long?=0,
    val name:String="",
    val islandExists:Boolean = false,
    val errorMessage:String?=null,
)