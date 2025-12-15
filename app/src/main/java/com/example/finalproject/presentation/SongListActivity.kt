package com.example.finalproject.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finalproject.domain.model.Song
import com.example.finalproject.presentation.shared.MiniPlayerBar
import com.example.finalproject.presentation.shared.SharedPlayerViewModel
import com.example.finalproject.presentation.songlist.SongListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sharedViewModel: SharedPlayerViewModel = hiltViewModel()
            
            Box(modifier = Modifier.fillMaxSize()) {
                SongListScreen(
                    onSongClick = { songs: List<Song>, position: Int ->
                        // Используем SharedPlayerViewModel для управления плеером
                        sharedViewModel.play(songs, position)
                        val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                        intent.putParcelableArrayListExtra("songList", ArrayList(songs))
                        intent.putExtra("position", position)
                        startActivity(intent)
                    }
                )
                
                // Mini Player Bar внизу
                MiniPlayerBar(
                    sharedViewModel = sharedViewModel,
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomCenter)
                        .padding(bottom = 0.dp)
                )
            }
        }
    }
}