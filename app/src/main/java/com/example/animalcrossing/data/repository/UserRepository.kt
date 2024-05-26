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

    suspend fun getUsers(): List<User> {
        return apiRepository.getUsers()
    }

    suspend fun getFriends(): List<User> {
        return apiRepository.getFriends()
    }

    suspend fun changeUsername(newUsername: String) {
        apiRepository.changeUsername(newUsername)
    }

    suspend fun changeDreamCode(newDreamCode: String) {
        apiRepository.changeDreamCode(newDreamCode)
    }

    suspend fun getFilteredUsers(search: String): List<User> {
        return apiRepository.getFilteredUsers(search)
    }

    suspend fun unfollowUser(uid: String) {
        apiRepository.unfollowUser(uid)
    }

    suspend fun followUser(uid: String) {
        apiRepository.followUser(uid)
    }

}