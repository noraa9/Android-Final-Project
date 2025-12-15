package com.example.finalproject.presentation.songlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalproject.domain.model.Song
import com.example.finalproject.domain.repository.SongRepository
import com.example.finalproject.domain.usecase.GetSongsUseCase
import com.example.finalproject.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SongListTab {
    ALL, FAVORITE
}

data class SongListUiState(
    val allSongs: List<Song> = emptyList(),
    val favoriteSongs: List<Song> = emptyList(),
    val currentTab: SongListTab = SongListTab.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class SongListViewModel @Inject constructor(
    private val getSongsUseCase: GetSongsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val songRepository: SongRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SongListUiState())
    val uiState: StateFlow<SongListUiState> = _uiState.asStateFlow()
    
    val displayedSongs: List<Song>
        get() {
            val songs = when (_uiState.value.currentTab) {
                SongListTab.ALL -> _uiState.value.allSongs
                SongListTab.FAVORITE -> _uiState.value.favoriteSongs
            }
            
            // Применяем поисковый фильтр
            val query = _uiState.value.searchQuery.lowercase().trim()
            return if (query.isEmpty()) {
                songs
            } else {
                songs.filter { song ->
                    song.title?.lowercase()?.contains(query) == true ||
                    song.artist?.lowercase()?.contains(query) == true
                }
            }
        }
    
    fun loadSongs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val songs = getSongsUseCase()
                // Загружаем избранные для обновления статуса
                val favoriteIds = songRepository.getFavoriteSongs().first().map { it.id }.toSet()
                val songsWithFavorites = songs.map { song ->
                    song.copy(isFavorite = favoriteIds.contains(song.id))
                }
                _uiState.value = _uiState.value.copy(
                    allSongs = songsWithFavorites,
                    isLoading = false
                )
                updateFavoriteSongs()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    init {
        // Следим за изменениями избранного в реальном времени
        songRepository.getFavoriteSongs()
            .onEach { favorites ->
                _uiState.value = _uiState.value.copy(favoriteSongs = favorites)
                // Обновляем статус избранного во всех песнях
                val favoriteIds = favorites.map { it.id }.toSet()
                val updatedAllSongs = _uiState.value.allSongs.map { song ->
                    song.copy(isFavorite = favoriteIds.contains(song.id))
                }
                _uiState.value = _uiState.value.copy(allSongs = updatedAllSongs)
            }
            .launchIn(viewModelScope)
    }
    
    fun updateFavoriteSongs() {
        viewModelScope.launch {
            val favorites = songRepository.getFavoriteSongs().first()
            _uiState.value = _uiState.value.copy(favoriteSongs = favorites)
            // Обновляем статус избранного во всех песнях
            val favoriteIds = favorites.map { it.id }.toSet()
            val updatedAllSongs = _uiState.value.allSongs.map { song ->
                song.copy(isFavorite = favoriteIds.contains(song.id))
            }
            _uiState.value = _uiState.value.copy(allSongs = updatedAllSongs)
        }
    }
    
    fun setCurrentTab(tab: SongListTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }
    
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            toggleFavoriteUseCase(song)
            updateFavoriteSongs()
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}


