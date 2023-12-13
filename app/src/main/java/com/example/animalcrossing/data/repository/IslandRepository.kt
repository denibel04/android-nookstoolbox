package com.example.animalcrossing.data.repository


import android.util.Log
import com.example.animalcrossing.data.db.IslandDBRepository
import com.example.animalcrossing.data.db.IslandEntity
import com.example.animalcrossing.data.db.VillagerEntity
import com.example.animalcrossing.data.db.asFish
import com.example.animalcrossing.data.db.asIsland
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IslandRepository @Inject constructor(
    private val dbRepository: IslandDBRepository
) {
    val island: Flow<Island?> = dbRepository.island
        .map { it?.asIsland() }

    suspend fun addIsland(name: String) {
        val newIsland = IslandEntity(name = name)
        val id = dbRepository.insert(newIsland)
    }
    suspend fun deleteIsland(id:Long) {
        val id = dbRepository.delete(id)
    }

    suspend fun renameIsland(id:Long, name: String) {
        Log.d("RENAME", "Renaming island $id to $name")
        val id = dbRepository.rename(id, name)
    }

}