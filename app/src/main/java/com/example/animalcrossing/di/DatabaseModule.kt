package com.example.animalcrossing.di

import android.content.Context
import com.example.animalcrossing.data.db.AcnhDao
import com.example.animalcrossing.data.db.AcnhDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideAcnhDatabase(@ApplicationContext context: Context): AcnhDatabase {
        return AcnhDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideAcnhDao(database: AcnhDatabase): AcnhDao {
        return database.acnhDao()
    }
}