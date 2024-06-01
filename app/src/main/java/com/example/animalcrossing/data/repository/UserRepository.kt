package com.example.animalcrossing.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
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
    private val dbRepository: ProfileDBRepository,
    private val context: Context
    ) {

    val profile: Flow<User>
        get() {
            val profile: Flow<User> = dbRepository.profile.map { profileEntity ->
                profileEntity.asUser() ?: User()
            }
            return profile
        }

    suspend fun getCurrentUser(): User {
        val profile = dbRepository.getProfile().firstOrNull()
        return profile.asUser() ?: User()
    }

    suspend fun getUsers(): List<UserDetail> {
        if (isOnline()) {
            return apiRepository.getUsers()
        } else {
            showNoInternetToast()
            return emptyList()
        }
    }

    suspend fun getFriends(): List<UserDetail> {
        if (isOnline()) {
            return apiRepository.getFriends()
        } else {
            showNoInternetToast()
            return emptyList()
        }
    }

    suspend fun changeUsername(newUsername: User) {
        if (isOnline()) {
            apiRepository.changeUsername(newUsername.username)
            dbRepository.updateProfile(
                ProfileEntity(
                    newUsername.uid,
                    newUsername.email,
                    newUsername.username,
                    newUsername.profile_picture,
                    newUsername.dreamCode ?: "",
                    newUsername.followers ?: 0,
                    newUsername.following ?: 0
                )
            )
        } else {
            showNoInternetToast()
        }
    }

    suspend fun changeDreamCode(newDreamCode: User) {
        if (isOnline()) {
            apiRepository.changeDreamCode(newDreamCode.dreamCode ?: "")
            dbRepository.updateProfile(
                ProfileEntity(
                    newDreamCode.uid,
                    newDreamCode.email,
                    newDreamCode.username,
                    newDreamCode.profile_picture,
                    newDreamCode.dreamCode ?: "",
                    newDreamCode.followers ?: 0,
                    newDreamCode.following ?: 0
                )
            )
        } else {
            showNoInternetToast()
        }
    }

    suspend fun changeProfilePicture(profilePicture: User) {
        dbRepository.updateProfile(
            ProfileEntity(
                profilePicture.uid,
                profilePicture.email,
                profilePicture.username,
                profilePicture.profile_picture,
                profilePicture.dreamCode ?: "",
                profilePicture.followers ?: 0,
                profilePicture.following ?: 0
            )
        )
    }

    suspend fun getFilteredUsers(search: String): List<UserDetail> {
        if (isOnline()) {
            return apiRepository.getFilteredUsers(search)
        } else {
            showNoInternetToast()
            return emptyList()
        }
    }

    suspend fun unfollowUser(uid: String) {
        if (isOnline()) {
            apiRepository.unfollowUser(uid)
        } else {
            showNoInternetToast()
        }
    }

    suspend fun followUser(uid: String) {
        if (isOnline()) {
            apiRepository.followUser(uid)
        } else {
            showNoInternetToast()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showNoInternetToast() {
        Toast.makeText(
            context,
            "No hay conexión a Internet. Por favor, comprueba tu conexión e inténtalo de nuevo.",
            Toast.LENGTH_SHORT
        ).show()
    }
}