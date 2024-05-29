package com.example.animalcrossing.data.db

import androidx.annotation.WorkerThread
import com.example.animalcrossing.data.repository.Villager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IslandDBRepository  @Inject constructor(private val acnhDao: AcnhDao) {

    val island: Flow<IslandEntity> = acnhDao.getIsland()

    val islandWithVillagers: Flow<IslandWithVillagers> = acnhDao.getIslandWithVillagers()


    @WorkerThread
    suspend fun searchVillagers(query: String): Flow<List<VillagerEntity>> {
        val searchQuery = "${query}%"
        return acnhDao.searchVillagers(searchQuery)
    }

    @WorkerThread
    suspend fun insert (island: IslandEntity) {
        acnhDao.insertIsland(island)
    }

    @WorkerThread
    suspend fun delete (islandId: Long) {
        val island = IslandEntity(islandId = islandId, name = "")
        acnhDao.deleteIsland(island)
    }

    @WorkerThread
    suspend fun rename(islandId: Long, newName:String) {
        return acnhDao.renameIsland(islandId, newName)
    }

    @WorkerThread
    suspend fun addVillagerToIsland(name: String, islandId: Long) {
        val crossRef = IslandVillagerCrossRef( islandId, name)
        acnhDao.addVillagerToIsland(crossRef)
    }

    @WorkerThread
    suspend fun deleteVillagerFromIsland(name: String, islandId: Long) {
        val crossRef = IslandVillagerCrossRef( islandId, name)
        acnhDao.deleteVillagerFromIsland(crossRef)
    }

    @WorkerThread
    suspend fun updateVillagerFromIsland(name: String, islandId: Long) {
        val crossRef = IslandVillagerCrossRef( islandId, name)
        acnhDao.updateVillagerFromIsland(crossRef)
    }

    @WorkerThread
    suspend fun deleteAllLoans() {
        acnhDao.deleteAllLoans()
    }




}