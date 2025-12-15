package com.example.finalproject.data.local.database


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finalproject.data.local.dao.FavoriteDao
import com.example.finalproject.data.local.entity.FavoriteSongEntity

@Database(
    entities = [FavoriteSongEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    
    abstract fun favoriteDao(): FavoriteDao
    
    companion object {
        const val DATABASE_NAME = "music_database"
    }
}

