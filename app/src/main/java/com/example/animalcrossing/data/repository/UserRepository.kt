package com.example.animalcrossing.data.repository

import com.example.animalcrossing.data.db.ProfileDBRepository
import com.example.animalcrossing.data.db.ProfileEntity
import com.example.animalcrossing.data.db.asLoan
import com.example.animalcrossing.data.db.asUser
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.firebase.UserDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiRepository: AcnhFirebaseRepository,
    private val dbRepository: ProfileDBRepository
) {

    val profile: Flow<User>
        get() {
            val profile:Flow<User> = dbRepository.profile.map {profileEntity ->
                profileEntity.asUser() ?: User()
            }
            return profile
        }

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

    suspend fun changeUsername(newUsername: User) {
        apiRepository.changeUsername(newUsername.username)
        dbRepository.updateProfile(ProfileEntity(newUsername.uid, newUsername.email, newUsername.username, newUsername.profile_picture, newUsername.dreamCode ?: "", newUsername.followers ?: 0, newUsername.following ?: 0))
    }

    suspend fun changeDreamCode(newDreamCode: User) {
        apiRepository.changeDreamCode(newDreamCode.dreamCode ?: "")
        dbRepository.updateProfile(ProfileEntity(newDreamCode.uid, newDreamCode.email, newDreamCode.username, newDreamCode.profile_picture, newDreamCode.dreamCode ?: "", newDreamCode.followers ?: 0, newDreamCode.following ?: 0))
    }

    suspend fun changeProfilePicture(profilePicture: User) {
        dbRepository.updateProfile(ProfileEntity(profilePicture.uid, profilePicture.email, profilePicture.username, profilePicture.profile_picture, profilePicture.dreamCode ?: "", profilePicture.followers ?: 0, profilePicture.following ?: 0))
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