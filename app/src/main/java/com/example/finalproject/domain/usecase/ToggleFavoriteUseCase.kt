package com.example.finalproject.domain.usecase

import com.example.finalproject.domain.model.Song
import com.example.finalproject.domain.repository.SongRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(song: Song) {
        if (song.isFavorite) {
            repository.removeFromFavorites(song.id)
        } else {
            repository.addToFavorites(song)
        }
    }
}

