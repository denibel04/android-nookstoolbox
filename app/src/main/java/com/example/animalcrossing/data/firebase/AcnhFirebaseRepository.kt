package com.example.animalcrossing.data.firebase

import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AcnhFirebaseRepository @Inject constructor(private val service: FirebaseService){
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

    suspend fun renameIsland(name: String) {
        service.renameIsland(name)
    }


    suspend fun deleteIsland() {
        service.deleteIsland()
    }

    suspend fun addVillagerToIsland(name: String) {
        service.addVillagerToIsland(name)
    }

    suspend fun deleteVillagerFromIsland(name: String) {
        service.deleteVillagerFromIsland(name)
    }

    suspend fun createLoan(loan: LoansEntity):String {
        return service.createLoan(loan)
    }

    suspend fun editLoan(loan: Loan) {
        service.editLoan(loan)
    }

    suspend fun deleteLoan(firebaseId: String) {
        service.deleteLoan(firebaseId)
    }


    suspend fun getCurrentUser(): User {
        return service.getCurrentUser()
    }
}

