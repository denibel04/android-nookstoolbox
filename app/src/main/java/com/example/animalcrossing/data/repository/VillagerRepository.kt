package com.example.animalcrossing.data.repository

import android.util.Log
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

@Singleton
class VillagerRepository @Inject constructor(
    private val dbRepository: VillagerDBRepository,
    private val apiRepository: AcnhFirebaseRepository
) {
    val villager: Flow<List<Villager>>
        get() {
            val list = dbRepository.allVillagers.map { it.asVillager() }
            return list
        }

    suspend fun refreshList() {
        withContext(Dispatchers.IO) {
            if (dbRepository.isVillagersTableEmpty()) {
                val apiAcnh = apiRepository.getAll()
                dbRepository.insert(apiAcnh.asEntityModel())
            }
        }
    }

    suspend fun getVillager(name:String):Flow<VillagerEntity> {
        return dbRepository.getVillager(name)
    }

}