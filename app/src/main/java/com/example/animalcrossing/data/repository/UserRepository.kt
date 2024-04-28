package com.example.animalcrossing.data.repository

import com.example.animalcrossing.data.api.AcnhApiRepository
import com.example.animalcrossing.data.api.asEntityModel
import com.example.animalcrossing.data.db.VillagerDBRepository
import com.example.animalcrossing.data.db.VillagerEntity
import com.example.animalcrossing.data.db.asVillager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiRepository: AcnhApiRepository
) {

    suspend fun getCurrentUser(): User {
        return apiRepository.getCurrentUser()
    }

}