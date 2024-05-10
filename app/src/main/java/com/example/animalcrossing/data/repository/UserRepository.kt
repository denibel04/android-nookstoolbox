package com.example.animalcrossing.data.repository

import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiRepository: AcnhFirebaseRepository
) {

    suspend fun getCurrentUser(): Flow<User?> {
        return apiRepository.getCurrentUser()
    }

}