package com.example.animalcrossing.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.animalcrossing.R
import com.example.animalcrossing.data.db.ProfileDBRepository
import com.example.animalcrossing.data.db.ProfileEntity
import com.example.animalcrossing.data.db.asUser
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.firebase.UserDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing user-related operations, integrating local database operations
 * with remote API interactions.
 *
 * This repository handles operations related to user profiles and interactions:
 * - Fetching the user's profile from local database.
 * - Fetching detailed user information from the API.
 * - Managing user relationships such as friends, followers, and following.
 * - Updating user profile information such as username, dream code, and profile picture.
 * - Performing search operations for users.
 *
 * @property apiRepository The repository for accessing remote Firebase API operations.
 * @property dbRepository The repository for accessing local database operations.
 * @property context The application context for accessing system services like connectivity.
 */
@Singleton
class UserRepository @Inject constructor(
    private val apiRepository: AcnhFirebaseRepository,
    private val dbRepository: ProfileDBRepository,
    private val context: Context
) {

    /**
     * Flow representing the user's profile fetched from the local database.
     * Maps the [ProfileEntity] to [User]. Returns a default empty [User] if no profile exists.
     */
    val profile: Flow<User>
        get() {
            return dbRepository.profile.map { profileEntity ->
                profileEntity.asUser() ?: User()
            }
        }

    /**
     * Retrieves a list of users from the API.
     *
     * @return List of [UserDetail] objects representing users.
     */
    suspend fun getUsers(): List<UserDetail> {
        return if (isOnline()) {
            apiRepository.getUsers()
        } else {
            showNoInternetToast()
            emptyList()
        }
    }

    /**
     * Retrieves detailed information of a user based on UID from the API.
     *
     * @param uid The UID of the user.
     * @return [UserProfileDetail] containing detailed user information.
     */
    suspend fun getUserDetail(uid: String): UserProfileDetail {
        return if (isOnline()) {
            apiRepository.getUserDetail(uid)
        } else {
            showNoInternetToast()
            UserProfileDetail("", "", "", "")
        }
    }

    /**
     * Retrieves a list of friends from the API.
     *
     * @return List of [UserDetail] objects representing friends.
     */
    suspend fun getFriends(): List<UserDetail> {
        return if (isOnline()) {
            apiRepository.getFriends()
        } else {
            showNoInternetToast()
            emptyList()
        }
    }

    /**
     * Retrieves a list of followers from the API.
     *
     * @return List of [UserDetail] objects representing followers.
     */
    suspend fun getFollowers(): List<UserDetail> {
        return if (isOnline()) {
            apiRepository.getFollowers()
        } else {
            emptyList()
        }
    }

    /**
     * Retrieves a list of users the current user is following from the API.
     *
     * @return List of [UserDetail] objects representing users being followed.
     */
    suspend fun getFollowing(): List<UserDetail> {
        return if (isOnline()) {
            apiRepository.getFollowing()
        } else {
            emptyList()
        }
    }

    /**
     * Changes the username of the current user.
     *
     * @param newUsername The new [User] object containing the updated username.
     */
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

    /**
     * Changes the dream code of the current user.
     *
     * @param newDreamCode The new [User] object containing the updated dream code.
     */
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

    /**
     * Changes the profile picture of the current user.
     *
     * @param profilePicture The new [User] object containing the updated profile picture.
     */
    suspend fun changeProfilePicture(profilePicture: User) {
        if (isOnline()) {
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
        } else {
            showNoInternetToast()
        }

    }

    /**
     * Retrieves a filtered list of users based on search query from the API.
     *
     * @param search The search query.
     * @return List of [UserDetail] objects matching the search criteria.
     */
    suspend fun getFilteredUsers(search: String): List<UserDetail> {
        return if (isOnline()) {
            apiRepository.getFilteredUsers(search)
        } else {
            showNoInternetToast()
            emptyList()
        }
    }

    /**
     * Unfollows a user based on UID.
     *
     * @param uid The UID of the user to unfollow.
     */
    suspend fun unfollowUser(uid: String) {
        if (isOnline()) {
            apiRepository.unfollowUser(uid)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Follows a user based on UID.
     *
     * @param uid The UID of the user to follow.
     */
    suspend fun followUser(uid: String) {
        if (isOnline()) {
            apiRepository.followUser(uid)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Updates the current user's profile information from the API.
     */
    suspend fun updateUser() {
        if (isOnline()) {
            val profile = apiRepository.getCurrentUser()
            val profileEntity = ProfileEntity(
                profile.uid,
                profile.email,
                profile.username,
                profile.profile_picture,
                profile.dreamCode.orEmpty(),
                profile.followers?.size ?: 0,
                profile.following?.size ?: 0
            )
            dbRepository.insert(profileEntity)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Checks if the device is currently online.
     *
     * @return True if the device has internet connectivity, false otherwise.
     */
    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Displays a toast message indicating no internet connection.
     */
    private fun showNoInternetToast() {
        Toast.makeText(
            context,
            context.getString(R.string.no_internet_connection),
            Toast.LENGTH_SHORT
        ).show()
    }
}