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
    /**
     * Inserts a list of villagers into the database. If there is a conflict,
     * replaces the existing data.
     *
     * @param listVillagerEntity List of VillagerEntity objects to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVillagers(listVillagerEntity: List<VillagerEntity>)

    /**
     * Retrieves all villagers from the database as a flow of lists.
     *
     * @return Flow emitting a list of all VillagerEntity objects.
     */
    @Query("SELECT * FROM villager")
    fun getAllVillagers(): Flow<List<VillagerEntity>>

    /**
     * Retrieves a specific villager from the database based on the name.
     *
     * @param name Name of the villager to retrieve.
     * @return Flow emitting the VillagerEntity object with the specified name.
     */
    @Query("SELECT * FROM villager WHERE name=:name")
    fun getVillager(name: String): Flow<VillagerEntity>

    /**
     * Searches for villagers whose name contains the specified search query.
     *
     * @param searchQuery Query string to search for in the villager names.
     * @return Flow emitting a list of VillagerEntity objects matching the search query.
     */
    @Query("SELECT * FROM villager WHERE name LIKE :searchQuery")
    fun searchVillagers(searchQuery: String): Flow<List<VillagerEntity>>

    /**
     * Returns the number of villagers in the database.
     *
     * @return Total count of villagers in the database.
     */
    @Query("SELECT COUNT(*) FROM villager")
    fun count(): Int

    /**
     * Inserts a loan entity into the database. If there is a conflict,
     * replaces the existing data.
     *
     * @param loansEntity LoansEntity object to insert or replace.
     * @return Long representing the row ID of the inserted loan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loansEntity: LoansEntity): Long

    /**
     * Retrieves all loans from the database as a flow of lists.
     *
     * @return Flow emitting a list of all LoansEntity objects.
     */
    @Query("SELECT * FROM loans")
    fun getAllLoans(): Flow<List<LoansEntity>>

    /**
     * Retrieves a specific loan from the database based on the Firebase ID.
     *
     * @param firebaseId Firebase ID of the loan to retrieve.
     * @return Flow emitting the LoansEntity object with the specified Firebase ID.
     */
    @Query("SELECT * FROM loans WHERE firebaseId=:firebaseId")
    fun getLoan(firebaseId: String): Flow<LoansEntity>

    /**
     * Deletes a loan entity from the database.
     *
     * @param loan LoansEntity object to delete.
     */
    @Delete
    suspend fun deleteLoan(loan: LoansEntity)

    /**
     * Updates a loan entity in the database.
     *
     * @param loansEntity LoansEntity object with updated values.
     */
    @Update
    suspend fun updateLoan(loansEntity: LoansEntity)

    // ISLAND

    /**
     * Inserts an island entity into the database. If there is a conflict,
     * replaces the existing data.
     *
     * @param island IslandEntity object to insert or replace.
     * @return Long representing the row ID of the inserted island.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIsland(island: IslandEntity): Long

    /**
     * Retrieves the island entity from the database.
     *
     * @return Flow emitting the IslandEntity object.
     */
    @Query("SELECT * FROM island")
    fun getIsland(): Flow<IslandEntity>

    /**
     * Deletes an island entity from the database.
     *
     * @param island IslandEntity object to delete.
     */
    @Delete
    suspend fun deleteIsland(island: IslandEntity)

    /**
     * Renames an island in the database.
     *
     * @param islandId ID of the island to rename.
     * @param name New name for the island.
     */
    @Query("UPDATE island SET name = :name WHERE islandId = :islandId")
    suspend fun renameIsland(islandId: Long, name: String)

    // ISLAND WITH VILLAGERS

    /**
     * Retrieves an island entity from the database along with its associated villagers.
     *
     * @return Flow emitting IslandWithVillagers object.
     */
    @Transaction
    @Query("SELECT * FROM Island")
    fun getIslandWithVillagers(): Flow<IslandWithVillagers>

    /**
     * Inserts a cross-reference entity linking a villager to an island.
     *
     * @param islandVillagerCrossRef IslandVillagerCrossRef object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addVillagerToIsland(islandVillagerCrossRef: IslandVillagerCrossRef)

    /**
     * Deletes a cross-reference entity linking a villager from an island.
     *
     * @param islandVillagerCrossRef IslandVillagerCrossRef object to delete.
     */
    @Delete
    suspend fun deleteVillagerFromIsland(islandVillagerCrossRef: IslandVillagerCrossRef)

    /**
     * Updates a cross-reference entity linking a villager to an island.
     *
     * @param islandVillagerCrossRef IslandVillagerCrossRef object with updated values.
     */
    @Update
    suspend fun updateVillagerFromIsland(islandVillagerCrossRef: IslandVillagerCrossRef)

    // PROFILE

    /**
     * Inserts a profile entity into the database. If there is a conflict,
     * replaces the existing data.
     *
     * @param profile ProfileEntity object to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    /**
     * Retrieves the profile entity from the database.
     *
     * @return Flow emitting the ProfileEntity object.
     */
    @Query("SELECT * FROM profile")
    fun getProfile(): Flow<ProfileEntity>

    /**
     * Updates a profile entity in the database.
     *
     * @param profile ProfileEntity object with updated values.
     */
    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    // RESET ALL

    /**
     * Deletes all island entities from the database.
     */
    @Query("DELETE FROM island")
    suspend fun deleteAllIslands()

    /**
     * Deletes all loan entities from the database.
     */
    @Query("DELETE FROM loans")
    suspend fun deleteAllLoans()

    /**
     * Deletes all profile entities from the database.
     */
    @Query("DELETE FROM profile")
    suspend fun deleteAllProfiles()

    /**
     * Deletes all island-villager cross-reference entities from the database.
     */
    @Query("DELETE FROM island_villager_cross_ref")
    suspend fun deleteAllIslandVillagerCrossRefs()
}