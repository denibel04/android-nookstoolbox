package com.example.animalcrossing.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animalcrossing.data.repository.Fish
import kotlinx.coroutines.flow.Flow

@Dao
interface AcnhDao {
    // FISH
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVillagers(listVillagerEntity: List<VillagerEntity>)
    @Query("SELECT * FROM villager")
    fun getAllVillagers(): Flow<List<VillagerEntity>>
    @Query("SELECT * FROM villager WHERE name=:name")
    fun getVillager(name: String): Flow<VillagerEntity>


    // VILLAGER
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFish(listFishEntity: List<FishEntity>)
    @Query("SELECT * FROM fish")
    fun getAllFish(): Flow<List<FishEntity>>
    @Query("SELECT * FROM fish WHERE name=:name")
    fun getFish(name: String): Flow<FishEntity>
    @Query("SELECT isCaught FROM fish WHERE name = :fishName")
    fun getIsCaughtByName(fishName: String): Flow<Boolean?>

    // ISLAND
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIsland(island: IslandEntity):Long
    @Query ("SELECT * FROM island")
    fun getIsland(): Flow<IslandEntity>
    @Delete
    suspend fun deleteIsland(island: IslandEntity)
    @Query("UPDATE island SET name = :name WHERE islandId = :islandId")
    suspend fun renameIsland(islandId: Long, name: String)
}