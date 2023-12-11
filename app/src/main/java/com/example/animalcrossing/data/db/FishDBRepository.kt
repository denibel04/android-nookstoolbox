package com.example.animalcrossing.data.db

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FishDBRepository  @Inject constructor(private val acnhDao: AcnhDao) {

    val allFish: Flow<List<FishEntity>> = acnhDao.getAllFish()

    @WorkerThread
    suspend fun insert (listFishEntity: List<FishEntity>) {
        acnhDao.insertFish(listFishEntity)
    }

    @WorkerThread
    suspend fun getFish(name: String): Flow<FishEntity> {
        return acnhDao.getFish(name)
    }
}