package com.example.finalproject.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Long,
    val title: String?,
    val artist: String?,
    val data: String,
    val albumId: Long,
    val isFavorite: Boolean = false
) : Parcelable

