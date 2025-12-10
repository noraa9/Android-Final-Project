package com.example.finalproject.domain.repository

import com.example.finalproject.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    suspend fun getAllSongs(): List<Song>
    fun getFavoriteSongs(): Flow<List<Song>>
    suspend fun addToFavorites(song: Song)
    suspend fun removeFromFavorites(songId: Long)
    suspend fun isFavorite(songId: Long): Boolean
}

