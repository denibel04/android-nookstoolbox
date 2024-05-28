package com.example.animalcrossing.data.repository

import com.example.animalcrossing.data.db.ProfileDBRepository
import com.example.animalcrossing.data.db.asUser
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.firebase.UserDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiRepository: AcnhFirebaseRepository,
    private val dbRepository: ProfileDBRepository
) {

    suspend fun getCurrentUser(): User {
        val profile = dbRepository.getProfile().firstOrNull()
        return profile.asUser() ?: User()
    }

    suspend fun getUsers(): List<UserDetail> {
        return apiRepository.getUsers()
    }

    suspend fun getFriends(): List<UserDetail> {
        return apiRepository.getFriends()
    }

    suspend fun changeUsername(newUsername: String) {
        apiRepository.changeUsername(newUsername)
    }

    suspend fun changeDreamCode(newDreamCode: String) {
        apiRepository.changeDreamCode(newDreamCode)
    }

    suspend fun getFilteredUsers(search: String): List<UserDetail> {
        return apiRepository.getFilteredUsers(search)
    }

    suspend fun unfollowUser(uid: String) {
        apiRepository.unfollowUser(uid)
    }

    suspend fun followUser(uid: String) {
        apiRepository.followUser(uid)
    }

}