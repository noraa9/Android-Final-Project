package com.example.finalproject.service

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.finalproject.domain.model.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerManager @Inject constructor(
    private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
    private var currentSongList: List<Song> = emptyList()
    private var currentIndex: Int = 0
    private var listeners = mutableListOf<Player.Listener>()
    
    fun getPlayer(): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build()
            // Восстанавливаем listeners
            listeners.forEach { exoPlayer?.addListener(it) }
        }
        return exoPlayer!!
    }
    
    fun play(songs: List<Song>, index: Int) {
        val song = songs.getOrNull(index) ?: return
        val player = getPlayer()
        
        // Проверяем, играет ли уже эта песня
        val currentMediaItem = player.currentMediaItem
        val isSameSong = currentMediaItem?.localConfiguration?.uri?.toString() == song.data
        
        // Если играет та же песня, не перезагружаем её
        if (isSameSong && player.playbackState != Player.STATE_IDLE) {
            currentSongList = songs
            currentIndex = index
            return // Продолжаем играть с текущей позиции
        }
        
        // Иначе загружаем новую песню
        currentSongList = songs
        currentIndex = index
        player.stop()
        player.clearMediaItems()
        player.setMediaItem(MediaItem.fromUri(song.data))
        player.prepare()
        player.playWhenReady = true
    }
    
    fun pause() {
        exoPlayer?.pause()
    }
    
    fun play() {
        exoPlayer?.play()
    }
    
    fun stop() {
        exoPlayer?.stop()
    }
    
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        listeners.clear()
    }
    
    fun seekTo(position: Long) {
        exoPlayer?.seekTo(position)
    }
    
    fun next() {
        if (currentSongList.isEmpty()) return
        val nextIndex = (currentIndex + 1) % currentSongList.size
        play(currentSongList, nextIndex)
    }
    
    fun previous() {
        if (currentSongList.isEmpty()) return
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else currentSongList.size - 1
        play(currentSongList, prevIndex)
    }
    
    fun getCurrentSong(): Song? {
        return currentSongList.getOrNull(currentIndex)
    }
    
    fun getCurrentIndex(): Int = currentIndex
    
    fun getCurrentSongList(): List<Song> = currentSongList
    
    fun addListener(listener: Player.Listener) {
        listeners.add(listener)
        exoPlayer?.addListener(listener)
    }
    
    fun removeListener(listener: Player.Listener) {
        listeners.remove(listener)
        exoPlayer?.removeListener(listener)
    }
    
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying == true
    }
    
    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }
    
    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }
}

