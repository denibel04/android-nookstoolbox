package com.example.animalcrossing.data.api

import com.example.animalcrossing.data.repository.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcnhApiRepository @Inject constructor(private val service: ApiService){
    suspend fun getAll():List<VillagerDetail> {

        val simpleList = service.getAllVillagers()
        val villagerApiModel = simpleList.map {
            villagerListItem ->
            VillagerDetail(
                villagerListItem.name,
                villagerListItem.species,
                villagerListItem.personality,
                villagerListItem.image_url,
                villagerListItem.gender,
                villagerListItem.birthday_month,
                villagerListItem.birthday_day
            )

        }
        return villagerApiModel
    }

    fun createIsland(name: String) {
        service.createIsland(name);
    }

    suspend fun deleteIsland() {
        service.deleteIsland()
    }


    suspend fun getCurrentUser(): User {
        return service.getCurrentUser()
    }
}

