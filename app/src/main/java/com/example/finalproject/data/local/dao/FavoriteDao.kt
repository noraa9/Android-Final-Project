package com.example.finalproject.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.finalproject.data.local.entity.FavoriteSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    
    @Query("SELECT * FROM favorite_songs ORDER BY dateAdded DESC")
    fun getAllFavorites(): Flow<List<FavoriteSongEntity>>
    
    @Query("SELECT * FROM favorite_songs WHERE songId = :songId")
    suspend fun getFavoriteById(songId: Long): FavoriteSongEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteSongEntity)
    
    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteFavorite(songId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE songId = :songId)")
    suspend fun isFavorite(songId: Long): Boolean
}

