package com.example.animalcrossing.data.repository

import android.util.Log
import androidx.room.Transaction
import com.example.animalcrossing.data.db.AcnhDao
import com.example.animalcrossing.data.db.IslandDBRepository
import com.example.animalcrossing.data.db.IslandEntity
import com.example.animalcrossing.data.db.IslandVillagerCrossRef
import com.example.animalcrossing.data.db.ProfileEntity
import com.example.animalcrossing.data.firebase.AcnhFirebaseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchRepository @Inject constructor(
    private val dbRepository: AcnhDao,
    private val apiRepository: AcnhFirebaseRepository
) {

    suspend fun onStartApp() {
        deleteAll()
        fetchAll()
    }

    @Transaction
    suspend fun deleteAll() {
    dbRepository.deleteAllIslands()
    dbRepository.deleteAllProfiles()
    dbRepository.deleteAllLoans()
    dbRepository.deleteAllIslandVillagerCrossRefs()
    }

    suspend fun fetchAll() {
        val islandData = apiRepository.getIsland()
        if (islandData.name != "") {
            val islandEntity = IslandEntity(name = islandData.name)
            val islandId = dbRepository.insertIsland(islandEntity)
            islandData.villagers.forEach {villager ->
                val newVillager = IslandVillagerCrossRef(islandId, villager)
                dbRepository.addVillagerToIsland(newVillager)
            }
            val loans = apiRepository.getLoans()
            loans.forEach {loans ->
                dbRepository.insertLoan(loans)
            }
            var profile = apiRepository.getCurrentUser()
            Log.d("profile", profile.toString())

            val profileEntity = ProfileEntity(
                profile.uid,
                profile.email,
                profile.username,
                profile.profile_picture,
                profile.dreamCode ?: "",
                profile.followers?.size ?: 0,
                profile.following?.size ?: 0
            )
            dbRepository.insertProfile(profileEntity)
        }

    }

    suspend fun fetchOnLogin() {

    }
}