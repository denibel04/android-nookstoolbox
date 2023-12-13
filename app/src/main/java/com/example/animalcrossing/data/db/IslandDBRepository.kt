package com.example.animalcrossing.data.db

import androidx.annotation.WorkerThread
import com.example.animalcrossing.data.repository.Villager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IslandDBRepository  @Inject constructor(private val acnhDao: AcnhDao) {

    val island: Flow<IslandEntity> = acnhDao.getIsland()

    @WorkerThread
    suspend fun insert (island: IslandEntity) {
        acnhDao.insertIsland(island)
    }

    @WorkerThread
    suspend fun delete (islandId: Long) {
        val island = IslandEntity(islandId = islandId, name = "")
        acnhDao.deleteIsland(island)
    }

}