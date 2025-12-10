package com.example.finalproject.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey
    val songId: Long,
    val title: String?,
    val artist: String?,
    val data: String,
    val albumId: Long,
    val dateAdded: Long = System.currentTimeMillis()
)

