package com.example.animalcrossing.data.db

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileDBRepository @Inject constructor(private val acnhDao: AcnhDao) {

    @WorkerThread
    suspend fun insert (profile: ProfileEntity) {
        acnhDao.insertProfile(profile)
    }

    @WorkerThread
    suspend fun getProfile(): Flow<ProfileEntity> {
        return acnhDao.getProfile()
    }
}