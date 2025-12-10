package com.example.finalproject.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.finalproject.domain.model.Song
import com.example.finalproject.presentation.songlist.SongListScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SongListScreen(
                onSongClick = { songs: List<Song>, position: Int ->
                    val intent = Intent(this@MainActivity, PlayerActivity::class.java)
                    intent.putParcelableArrayListExtra("songList", ArrayList(songs))
                    intent.putExtra("position", position)
                    startActivity(intent)
                }
            )
        }
    }
}