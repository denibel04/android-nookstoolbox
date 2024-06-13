package com.example.animalcrossing.data.repository


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.example.animalcrossing.R
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.db.IslandDBRepository
import com.example.animalcrossing.data.db.IslandEntity
import com.example.animalcrossing.data.db.IslandWithVillagers
import com.example.animalcrossing.data.db.VillagerEntity
import com.example.animalcrossing.data.db.asIsland
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing islands and villagers, integrating local database operations
 * with remote API interactions.
 *
 * This repository handles operations related to islands and villagers:
 * - Fetching islands and villagers from local database.
 * - Adding, deleting, and renaming islands.
 * - Searching villagers by name.
 * - Adding and deleting villagers from islands.
 *
 * @property dbRepository The repository for accessing local database operations.
 * @property apiRepository The repository for accessing remote Firebase API operations.
 * @property context The application context for accessing system services like connectivity.
 */
@Singleton
class IslandRepository @Inject constructor(
    private val dbRepository: IslandDBRepository,
    private val apiRepository: AcnhFirebaseRepository,
    private val context: Context
) {
    /**
     * Flow representing the current island from the local database.
     * Maps the [IslandEntity] to [Island] for external usage.
     */
    val island: Flow<Island?> = dbRepository.island
        .map { it.asIsland() }

    /**
     * Flow representing the island with its associated villagers from the local database.
     */
    val islandWithVillagers: Flow<IslandWithVillagers?> = dbRepository.islandWithVillagers

    /**
     * Adds a new island locally and remotely if online.
     *
     * @param name The name of the island.
     * @param hemisphere The hemisphere of the island.
     */
    suspend fun addIsland(name: String, hemisphere: String) {
        if (isOnline()) {
            val newIsland = IslandEntity(name = name, hemisphere = hemisphere)
            apiRepository.createIsland(name, hemisphere)
            dbRepository.insert(newIsland)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Deletes an island locally and remotely if online.
     *
     * @param id The ID of the island to delete.
     */
    suspend fun deleteIsland(id: Long) {
        if (isOnline()) {
            apiRepository.deleteIsland()
            dbRepository.delete(id)
            dbRepository.deleteAllLoans()
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Renames an island locally and remotely if online.
     *
     * @param id The ID of the island to rename.
     * @param name The new name for the island.
     */
    suspend fun renameIsland(id: Long, name: String) {
        if (isOnline()) {
            apiRepository.renameIsland(name)
            dbRepository.rename(id, name)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Searches for villagers matching the specified query in the local database.
     *
     * @param query The query string to search for villagers.
     * @return Flow emitting a list of [VillagerEntity] matching the query.
     */
    fun searchVillagers(query: String): Flow<List<VillagerEntity>> {
        return dbRepository.searchVillagers(query)
    }

    /**
     * Adds a villager to an island locally and remotely if online.
     *
     * @param name The name of the villager to add.
     * @param islandId The ID of the island to which the villager should be added.
     */
    suspend fun addVillagerToIsland(name: String, islandId: Long) {
        if (isOnline()) {
            apiRepository.addVillagerToIsland(name)
            dbRepository.addVillagerToIsland(name, islandId)
        } else {
            showNoInternetToast()
        }
    }

    /**
     * Deletes a villager from an island locally and remotely if online.
     *
     * @param name The name of the villager to delete.
     * @param islandId The ID of the island from which the villager should be deleted.
     */
    suspend fun deleteVillagerFromIsland(name: String, islandId: Long) {
        if (isOnline()) {
            apiRepository.deleteVillagerFromIsland(name)
            dbRepository.deleteVillagerFromIsland(name, islandId)
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