package com.example.animalcrossing.data.api

import com.example.animalcrossing.data.repository.Fish
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcnhApiRepository @Inject constructor(private val service: ApiService){
    suspend fun getAll():List<VillagerDetail> {
        val simpleList = service.api.getAllVillagers("d6dfbf26-c471-4933-bd12-46940031ab6c",5,0)
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

    suspend fun getAllFish():List<FishDetail> {
        val simpleList = service.api.getAllFish("d6dfbf26-c471-4933-bd12-46940031ab6c",5,0)
        val fishApiModel = simpleList.map {
            fishListItem ->
            FishDetail(
                fishListItem.name,
                fishListItem.image_url,
                fishListItem.location,
                fishListItem.shadow_size,
                fishListItem.rarity
            )
        }
        return fishApiModel
    }
}

