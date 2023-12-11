package com.example.animalcrossing.data.repository

import com.example.animalcrossing.data.api.AcnhApiRepository
import com.example.animalcrossing.data.db.asFish
import com.example.animalcrossing.data.api.fishAsEntityModel
import com.example.animalcrossing.data.db.FishDBRepository
import com.example.animalcrossing.data.db.FishEntity
import com.example.animalcrossing.data.db.VillagerDBRepository
import com.example.animalcrossing.data.db.VillagerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FishRepository @Inject constructor(
    private val dbRepository: FishDBRepository,
    private val apiRepository: AcnhApiRepository
) {
    val fish: Flow<List<Fish>>
        get() {
            val list = dbRepository.allFish.map { it.asFish() }
            return list
        }

    suspend fun refreshList() {
        withContext(Dispatchers.IO) {
            val apiAcnh = apiRepository.getAllFish()
            dbRepository.insert(apiAcnh.fishAsEntityModel())
        }
    }

    suspend fun getFish(name:String):Flow<FishEntity> {
        return dbRepository.getFish(name)
    }

}