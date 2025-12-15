package com.example.finalproject.data.repository

import android.content.Context
import android.provider.MediaStore
import com.example.finalproject.data.local.dao.FavoriteDao
import com.example.finalproject.data.local.entity.FavoriteSongEntity
import com.example.finalproject.domain.model.Song
import com.example.finalproject.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val context: Context
) : SongRepository {
    
    override suspend fun getAllSongs(): List<Song> {
        val favoriteEntities = favoriteDao.getAllFavorites().first()
        val favoriteIds = favoriteEntities.map { it.songId }.toSet()
        
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        
        // Разрешенные расширения музыкальных файлов
        val musicExtensions = setOf(".mp3", ".m4a", ".flac", ".wav", ".ogg", ".aac", ".wma", ".opus")
        
        // Папки, которые нужно исключить (системные папки с аудио)
        val excludedFolders = setOf(
            "/Notifications/",
            "/Ringtones/",
            "/Alarms/",
            "/notifications/",
            "/ringtones/",
            "/alarms/",
            "/Android/data/",
            "/Android/obb/"
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}!=0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        
        context.contentResolver.query(
            uri, projection, selection, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            
            while (cursor.moveToNext()) {
                val data = cursor.getString(dataCol)
                
                // Пропускаем файлы из системных папок
                if (data == null || excludedFolders.any { data.contains(it, ignoreCase = true) }) {
                    continue
                }
                
                // Пропускаем файлы без подходящего расширения
                val fileExtension = data.substringAfterLast('.', "").lowercase()
                if (fileExtension.isEmpty() || !musicExtensions.contains(".$fileExtension")) {
                    continue
                }
                
                // Пропускаем слишком короткие файлы (меньше 10 секунд) - обычно это не музыка
                val duration = cursor.getLong(durationCol)
                if (duration > 0 && duration < 10000) { // меньше 10 секунд
                    continue
                }
                
                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol)
                val artist = cursor.getString(artistCol)
                val album = cursor.getLong(albumCol)
                val isFavorite = favoriteIds.contains(id)
                
                songs.add(Song(id, title, artist, data, album, isFavorite))
            }
        }
        return songs
    }
    
    override fun getFavoriteSongs(): Flow<List<Song>> {
        return favoriteDao.getAllFavorites().map { entities ->
            entities.map { entity ->
                Song(
                    id = entity.songId,
                    title = entity.title,
                    artist = entity.artist,
                    data = entity.data,
                    albumId = entity.albumId,
                    isFavorite = true
                )
            }
        }
    }
    
    override suspend fun addToFavorites(song: Song) {
        val entity = FavoriteSongEntity(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            data = song.data,
            albumId = song.albumId
        )
        favoriteDao.insertFavorite(entity)
    }
    
    override suspend fun removeFromFavorites(songId: Long) {
        favoriteDao.deleteFavorite(songId)
    }
    
    override suspend fun isFavorite(songId: Long): Boolean {
        return favoriteDao.isFavorite(songId)
    }
}

