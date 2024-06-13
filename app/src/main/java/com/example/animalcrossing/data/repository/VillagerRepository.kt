package com.example.animalcrossing.data.repository

import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import com.example.animalcrossing.data.firebase.asEntityModel
import com.example.animalcrossing.data.db.VillagerDBRepository
import com.example.animalcrossing.data.db.VillagerEntity
import com.example.animalcrossing.data.db.asVillager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing villager-related operations, integrating local database operations
 * with remote API interactions.
 *
 * This repository handles operations related to villagers:
 * - Fetching a list of all villagers from the local database.
 * - Refreshing the list of villagers from the remote API and updating the local database.
 * - Fetching details of a specific villager by name from the local database.
 *
 * @property dbRepository The repository for accessing local villager database operations.
 * @property apiRepository The repository for accessing remote Firebase API operations.
 */
@Singleton
class VillagerRepository @Inject constructor(
    private val dbRepository: VillagerDBRepository,
    private val apiRepository: AcnhFirebaseRepository
) {
    /**
     * Flow representing the list of all villagers fetched from the local database.
     * Maps [VillagerEntity] objects to [Villager] domain models.
     */
    val villager: Flow<List<Villager>>
        get() {
            return dbRepository.allVillagers.map { it.asVillager() }
        }

    /**
     * Refreshes the list of villagers from the remote API and updates the local database
     * if the local database is initially empty.
     */
    suspend fun refreshList() {
        withContext(Dispatchers.IO) {
            if (dbRepository.isVillagersTableEmpty()) {
                val apiAcnh = apiRepository.getAll()
                dbRepository.insert(apiAcnh.asEntityModel())
            }
        }
    }

    /**
     * Retrieves the details of a specific villager by name from the local database.
     *
     * @param name The name of the villager to retrieve.
     * @return Flow emitting the [VillagerEntity] object representing the villager.
     */
    suspend fun getVillager(name: String): Flow<VillagerEntity> {
        return dbRepository.getVillager(name)
    }
}