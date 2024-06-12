package com.example.animalcrossing.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.Transaction
import com.example.animalcrossing.data.db.AcnhDao
import com.example.animalcrossing.data.db.IslandEntity
import com.example.animalcrossing.data.db.IslandVillagerCrossRef
import com.example.animalcrossing.data.db.ProfileEntity
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles the synchronization of data between the local database and Firebase.
 *
 * @property dbRepository The DAO for accessing local database operations.
 * @property apiRepository The repository for accessing Firebase operations.
 * @property context The context for accessing system services.
 */
@Singleton
class FetchRepository @Inject constructor(
    private val dbRepository: AcnhDao,
    private val apiRepository: AcnhFirebaseRepository,
    private val context: Context
) {

    /**
     * Method to be called when the app starts. If there is an internet connection,
     * it deletes all local data and fetches fresh data from Firebase.
     */
    suspend fun onStartApp() {
        if (isOnline()) {
            deleteAll()
            fetchAll()
        }
    }

    /**
     * Deletes all data from the tables related to islands, profiles, loans, and island-villager cross references.
     */
    @Transaction
    suspend fun deleteAll() {
        dbRepository.deleteAllIslands()
        dbRepository.deleteAllProfiles()
        dbRepository.deleteAllLoans()
        dbRepository.deleteAllIslandVillagerCrossRefs()
    }

    /**
     * Fetches all necessary data from Firebase and inserts it into the local database.
     */
    private suspend fun fetchAll() {
        val islandData = apiRepository.getIsland()
        if (islandData.name.isNotEmpty()) {
            val islandEntity = IslandEntity(name = islandData.name, hemisphere = islandData.hemisphere)
            val islandId = dbRepository.insertIsland(islandEntity)
            islandData.villagers.forEach { villager ->
                val newVillager = IslandVillagerCrossRef(islandId, villager)
                dbRepository.addVillagerToIsland(newVillager)
            }
            val loans = apiRepository.getLoans()
            loans.forEach { loan ->
                dbRepository.insertLoan(loan)
            }
        }

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
        dbRepository.insertProfile(profileEntity)
    }

    /**
     * Checks if the device is connected to the internet.
     *
     * @return `true` if there is an internet connection, `false` otherwise.
     */
    private fun isOnline(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}