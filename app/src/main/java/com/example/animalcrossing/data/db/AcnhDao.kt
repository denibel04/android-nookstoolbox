package com.example.animalcrossing.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AcnhDao {
    // VILLAGER
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVillagers(listVillagerEntity: List<VillagerEntity>)
    @Query("SELECT * FROM villager")
    fun getAllVillagers(): Flow<List<VillagerEntity>>
    @Query("SELECT * FROM villager WHERE name=:name")
    fun getVillager(name: String): Flow<VillagerEntity>
    @Query("SELECT * FROM villager WHERE name LIKE :searchQuery")
    fun searchVillagers(searchQuery: String): Flow<List<VillagerEntity>>

    // LOAN
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loansEntity: LoansEntity)

    @Query("SELECT * FROM loans WHERE loanId=:loanId")
    fun getLoan(loanId: Long): Flow<LoansEntity>

    @Delete
    suspend fun deleteLoan(loan: LoansEntity)

    // ISLAND
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIsland(island: IslandEntity):Long
    @Query ("SELECT * FROM island")
    fun getIsland(): Flow<IslandEntity>
    @Delete
    suspend fun deleteIsland(island: IslandEntity)
    @Query("UPDATE island SET name = :name WHERE islandId = :islandId")
    suspend fun renameIsland(islandId: Long, name: String)

    // ISLAND W VILLAGERS
    @Transaction
    @Query("SELECT * FROM Island")
    fun getIslandWithVillagers(): Flow<IslandWithVillagers>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVillagerToIsland(islandVillagerCrossRef: IslandVillagerCrossRef)
    @Delete
    suspend fun deleteVillagerFromIsland(islandVillagerCrossRef: IslandVillagerCrossRef)
    @Update
    suspend fun updateVillagerFromIsland(islandVillagerCrossRef: IslandVillagerCrossRef)

    // ISLAND W LOANS
    @Transaction
    @Query("SELECT * FROM Island")
    fun getIslandWithLoans(): Flow<IslandWithLoans>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addLoanToIsland(islandLoansCrossRef: IslandLoansCrossRef)
    @Delete
    suspend fun deleteLoanFromIsland(islandLoansCrossRef: IslandLoansCrossRef)
    @Update
    suspend fun updateLoanFromIsland(islandLoansCrossRef: IslandLoansCrossRef)



}