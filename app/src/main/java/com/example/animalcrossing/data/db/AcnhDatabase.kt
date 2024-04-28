package com.example.animalcrossing.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [VillagerEntity::class, IslandEntity::class, IslandVillagerCrossRef::class], version = 1)
abstract class AcnhDatabase():RoomDatabase() {
    abstract fun acnhDao():AcnhDao

    companion object {
        @Volatile
        private var INSTANCE:AcnhDatabase?=null

        fun getInstance(context: Context):AcnhDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AcnhDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AcnhDatabase::class.java,
                "acnh7_db"
            ).build()
        }
    }
}