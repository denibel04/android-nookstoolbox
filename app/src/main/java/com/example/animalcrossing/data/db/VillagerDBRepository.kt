package com.example.animalcrossing.data.db

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VillagerDBRepository  @Inject constructor(private val acnhDao: AcnhDao) {

    val allVillagers: Flow<List<VillagerEntity>> = acnhDao.getAllVillagers()

    @WorkerThread
    suspend fun insert (listVillagerEntity: List<VillagerEntity>) {
        acnhDao.insertVillagers(listVillagerEntity)
    }

    @WorkerThread
    suspend fun getVillager(name: String): Flow<VillagerEntity> {
        return acnhDao.getVillager(name)
    }

    suspend fun isVillagersTableEmpty(): Boolean {
        return acnhDao.count() == 0
    }
}