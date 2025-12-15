package com.example.finalproject.presentation.shared

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.domain.model.Song
import com.example.finalproject.presentation.PlayerActivity
import com.example.finalproject.service.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SharedPlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val progress: Float = 0f
)

@HiltViewModel
class SharedPlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {
    
    private val _playerState = MutableStateFlow(SharedPlayerState())
    val playerState: StateFlow<SharedPlayerState> = _playerState.asStateFlow()
    
    private var positionUpdateJob: Job? = null
    private val playerListener = object : androidx.media3.common.Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            viewModelScope.launch {
                updatePlayerState()
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }
        }
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            viewModelScope.launch {
                updatePlayerState()
                if (playbackState == androidx.media3.common.Player.STATE_READY && playerManager.isPlaying()) {
                    startPositionUpdates()
                }
            }
        }
    }
    
    init {
        // Начинаем обновлять позицию, если плеер играет
        viewModelScope.launch {
            updatePlayerState()
        }
        
        // Слушаем изменения состояния плеера
        playerManager.addListener(playerListener)
    }
    
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = viewModelScope.launch {
            while (playerManager.isPlaying()) {
                updatePlayerState()
                delay(500)
            }
        }
    }
    
    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }
    
    private suspend fun updatePlayerState() {
        val currentSong = playerManager.getCurrentSong()
        val isPlaying = playerManager.isPlaying()
        val currentPosition = playerManager.getCurrentPosition()
        val duration = playerManager.getDuration()
        val progress = if (duration > 0) currentPosition.toFloat() / duration else 0f
        
        _playerState.value = SharedPlayerState(
            currentSong = currentSong,
            isPlaying = isPlaying,
            currentPosition = currentPosition,
            duration = duration,
            progress = progress
        )
    }
    
    fun play(songs: List<Song>, index: Int) {
        playerManager.play(songs, index)
        viewModelScope.launch {
            updatePlayerState()
            startPositionUpdates()
        }
    }
    
    fun togglePlayPause() {
        if (playerManager.isPlaying()) {
            playerManager.pause()
        } else {
            playerManager.play()
        }
        viewModelScope.launch {
            updatePlayerState()
        }
    }
    
    fun next() {
        playerManager.next()
        viewModelScope.launch {
            updatePlayerState()
        }
    }
    
    fun previous() {
        playerManager.previous()
        viewModelScope.launch {
            updatePlayerState()
        }
    }
    
    fun seekTo(position: Long) {
        playerManager.seekTo(position)
        viewModelScope.launch {
            updatePlayerState()
        }
    }
    
    fun openPlayerScreen(context: Context) {
        val songs = playerManager.getCurrentSongList()
        val index = playerManager.getCurrentIndex()
        if (songs.isNotEmpty()) {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putParcelableArrayListExtra("songList", ArrayList(songs))
            intent.putExtra("position", index)
            context.startActivity(intent)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPositionUpdates()
        playerManager.removeListener(playerListener)
    }
}

