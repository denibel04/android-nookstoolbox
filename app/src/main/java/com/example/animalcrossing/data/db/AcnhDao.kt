package com.example.animalcrossing.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animalcrossing.data.repository.Fish
import kotlinx.coroutines.flow.Flow

@Dao
interface AcnhDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVillagers(listVillagerEntity: List<VillagerEntity>)
    @Query("SELECT * FROM villager")
    fun getAllVillagers(): Flow<List<VillagerEntity>>
    @Query("SELECT * FROM villager WHERE name=:name")
    fun getVillager(name: String): Flow<VillagerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(listFishEntity: List<FishEntity>)
    @Query("SELECT * FROM fish")
    fun getAllFish(): Flow<List<FishEntity>>
    @Query("SELECT * FROM fish WHERE name=:name")
    fun getFish(name: String): Flow<FishEntity>


}