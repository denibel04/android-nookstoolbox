package com.example.animalcrossing.data.firebase

import com.example.animalcrossing.data.db.LoansEntity
import com.example.animalcrossing.data.repository.Loan
import com.example.animalcrossing.data.repository.UserProfileDetail
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

    suspend fun getIsland(): IslandDetail {
        return service.getIsland()
    }
    fun createIsland(name: String, hemisphere: String) {
        service.createIsland(name, hemisphere)
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

    suspend fun getLoans(): List<LoansEntity> {
        return service.getLoans()
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


    suspend fun getCurrentUser(): UserDetail {
        return service.getCurrentUser()
    }

    suspend fun getFriends(): List<UserDetail> {
        return service.getFriends()
    }

    suspend fun getFollowers(): List<UserDetail> {
        return service.getFollowers()
    }

    suspend fun getFollowing(): List<UserDetail> {
        return service.getFollowing()
    }

    suspend fun changeUsername(newUsername: String) {
        service.changeUsername(newUsername)
    }

    suspend fun changeDreamCode(newDreamCode: String) {
        service.changeDreamCode(newDreamCode)
    }

    suspend fun getUsers(): List<UserDetail> {
        return service.getUsers()
    }

    suspend fun getUserDetail(uid: String): UserProfileDetail {
        return service.getUserDetail(uid)
    }

    suspend fun getFilteredUsers(search: String): List<UserDetail> {
        return service.getFilteredUsers(search)
    }

    suspend fun unfollowUser(uid: String) {
        service.unfollowUser(uid)
    }

    suspend fun followUser(uid: String) {
        service.followUser(uid)
    }


    // FETCH




}

