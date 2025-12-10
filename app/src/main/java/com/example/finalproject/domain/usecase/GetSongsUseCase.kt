package com.example.finalproject.domain.usecase

import com.example.finalproject.domain.model.Song
import com.example.finalproject.domain.repository.SongRepository
import javax.inject.Inject

class GetSongsUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(): List<Song> {
        return repository.getAllSongs()
    }
}

