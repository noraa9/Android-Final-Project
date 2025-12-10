package com.example.finalproject.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.domain.model.Song
import com.example.finalproject.domain.repository.SongRepository
import com.example.finalproject.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val songRepository: SongRepository
) : ViewModel() {
    
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private var songList: List<Song> = emptyList()
    
    fun initialize(songs: List<Song>, initialIndex: Int) {
        songList = songs
        val song = songs.getOrNull(initialIndex)
        _playerState.value = _playerState.value.copy(
            currentIndex = initialIndex,
            currentSong = song,
            elapsed = 0L,
            duration = 0L,
            waveformProgress = 0f,
            isShuffle = false,
            isRepeat = false,
            shuffledList = emptyList()
        )
    }
    
    fun setCurrentSong(song: Song?, index: Int) {
        _playerState.value = _playerState.value.copy(
            currentSong = song,
            currentIndex = index
        )
    }
    
    fun setPlaying(isPlaying: Boolean) {
        _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
    }
    
    fun setShuffle(isShuffle: Boolean, shuffledList: List<Song>) {
        _playerState.value = _playerState.value.copy(
            isShuffle = isShuffle,
            shuffledList = shuffledList
        )
    }
    
    fun setRepeat(isRepeat: Boolean) {
        _playerState.value = _playerState.value.copy(isRepeat = isRepeat)
    }
    
    fun setElapsed(elapsed: Long) {
        _playerState.value = _playerState.value.copy(elapsed = elapsed)
    }
    
    fun setDuration(duration: Long) {
        _playerState.value = _playerState.value.copy(duration = duration)
    }
    
    fun setWaveformProgress(progress: Float) {
        _playerState.value = _playerState.value.copy(waveformProgress = progress)
    }
    
    fun next() {
        val list = if (_playerState.value.isShuffle) {
            _playerState.value.shuffledList
        } else {
            songList
        }
        val nextIndex = (_playerState.value.currentIndex + 1) % list.size
        _playerState.value = _playerState.value.copy(
            currentIndex = nextIndex,
            currentSong = list.getOrNull(nextIndex)
        )
    }
    
    fun previous() {
        val list = if (_playerState.value.isShuffle) {
            _playerState.value.shuffledList
        } else {
            songList
        }
        val prevIndex = if (_playerState.value.currentIndex - 1 < 0) {
            list.size - 1
        } else {
            _playerState.value.currentIndex - 1
        }
        _playerState.value = _playerState.value.copy(
            currentIndex = prevIndex,
            currentSong = list.getOrNull(prevIndex)
        )
    }
    
    fun toggleFavorite() {
        val currentSong = _playerState.value.currentSong ?: return
        viewModelScope.launch {
            // Оптимистичное обновление UI
            val newFavoriteStatus = !currentSong.isFavorite
            var updatedSong = currentSong.copy(isFavorite = newFavoriteStatus)
            _playerState.value = _playerState.value.copy(currentSong = updatedSong)
            
            // Обновляем в базе данных
            toggleFavoriteUseCase(currentSong)
            
            // Проверяем актуальный статус из базы (с небольшой задержкой для завершения транзакции)
            kotlinx.coroutines.delay(100)
            val isFavoriteNow = songRepository.isFavorite(currentSong.id)
            if (isFavoriteNow != newFavoriteStatus) {
                updatedSong = currentSong.copy(isFavorite = isFavoriteNow)
                _playerState.value = _playerState.value.copy(currentSong = updatedSong)
            }
        }
    }
}

