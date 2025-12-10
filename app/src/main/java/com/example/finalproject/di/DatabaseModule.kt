package com.example.finalproject.di

import android.content.Context
import androidx.room.Room
import com.example.finalproject.data.local.dao.FavoriteDao
import com.example.finalproject.data.local.database.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideMusicDatabase(
        @ApplicationContext context: Context
    ): MusicDatabase {
        return Room.databaseBuilder(
            context,
            MusicDatabase::class.java,
            MusicDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    fun provideFavoriteDao(database: MusicDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}

