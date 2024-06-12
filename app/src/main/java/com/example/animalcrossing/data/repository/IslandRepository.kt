package com.example.animalcrossing.data.repository


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
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

@Singleton
class IslandRepository @Inject constructor(
    private val dbRepository: IslandDBRepository,
    private val apiRepository: AcnhFirebaseRepository,
    private val context: Context
) {
    val island: Flow<Island?> = dbRepository.island
        .map { it.asIsland() }

    val islandWithVillagers:Flow<IslandWithVillagers?> = dbRepository.islandWithVillagers


    suspend fun addIsland(name: String, hemisphere: String) {
        if (isOnline()) {
            val newIsland = IslandEntity(name = name, hemisphere = hemisphere)
            apiRepository.createIsland(name, hemisphere)
            dbRepository.insert(newIsland)
        } else {
            showNoInternetToast()
        }
    }

    suspend fun deleteIsland(id: Long) {
        if (isOnline()) {
            apiRepository.deleteIsland()
            dbRepository.delete(id)
            dbRepository.deleteAllLoans()
        } else {
            showNoInternetToast()
        }
    }

    suspend fun renameIsland(id: Long, name: String) {
        if (isOnline()) {
            apiRepository.renameIsland(name)
            dbRepository.rename(id, name)
        } else {
            showNoInternetToast()
        }
    }

    suspend fun searchVillagers(query: String): Flow<List<VillagerEntity>> {
        return dbRepository.searchVillagers(query)
    }

    suspend fun addVillagerToIsland(name: String, islandId: Long) {
        if (isOnline()) {
            apiRepository.addVillagerToIsland(name)
            dbRepository.addVillagerToIsland(name, islandId)
        } else {
            showNoInternetToast()
        }
    }

    suspend fun deleteVillagerFromIsland(name: String, islandId: Long) {
        if (isOnline()) {
            apiRepository.deleteVillagerFromIsland(name)
            dbRepository.deleteVillagerFromIsland(name, islandId)
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