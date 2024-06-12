package com.example.animalcrossing.data.db

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileDBRepository @Inject constructor(private val acnhDao: AcnhDao) {

    val profile: Flow<ProfileEntity> = acnhDao.getProfile()

    @WorkerThread
    suspend fun insert (profile: ProfileEntity) {
        acnhDao.insertProfile(profile)
    }

    @WorkerThread
    suspend fun updateProfile(profile: ProfileEntity) {
        acnhDao.updateProfile(profile)
    }
}