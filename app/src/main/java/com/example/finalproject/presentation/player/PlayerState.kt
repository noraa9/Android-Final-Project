package com.example.finalproject.presentation.player

import com.example.finalproject.domain.model.Song

data class PlayerState(
    val currentSong: Song? = null,
    val currentIndex: Int = 0,
    val isPlaying: Boolean = false,
    val isShuffle: Boolean = false,
    val isRepeat: Boolean = false,
    val elapsed: Long = 0L,
    val duration: Long = 0L,
    val shuffledList: List<Song> = emptyList(),
    val waveformProgress: Float = 0f
)

