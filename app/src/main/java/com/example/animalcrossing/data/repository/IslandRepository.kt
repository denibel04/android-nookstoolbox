package com.example.animalcrossing.data.repository


import android.util.Log
import com.example.animalcrossing.data.db.IslandDBRepository
import com.example.animalcrossing.data.db.IslandEntity
import com.example.animalcrossing.data.db.IslandVillagerCrossRef
import com.example.animalcrossing.data.db.IslandWithVillagers
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

    val islandWithVillagers:Flow<IslandWithVillagers?> = dbRepository.islandWithVillagers


    suspend fun addIsland(name: String) {
        val newIsland = IslandEntity(name = name)
        val id = dbRepository.insert(newIsland)
    }
    suspend fun deleteIsland(id:Long) {
        val id = dbRepository.delete(id)
    }

    suspend fun renameIsland(id:Long, name: String) {
        val id = dbRepository.rename(id, name)
    }

    suspend fun searchVillagers(query: String): Flow<List<VillagerEntity>> {
        return dbRepository.searchVillagers(query)
    }

    suspend fun addVillagerToIsland(name: String, islandId: Long) {
        return dbRepository.addVillagerToIsland(name, islandId)
    }

    suspend fun deleteVillagerFromIsland(name: String, islandId: Long) {
        return dbRepository.deleteVillagerFromIsland(name, islandId)
    }

    suspend fun updateVillagerFromIsland(name: String, islandId: Long) {
        return dbRepository.updateVillagerFromIsland(name, islandId)
    }





}