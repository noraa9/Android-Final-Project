package com.example.finalproject.presentation.player

import android.content.ContentUris
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.finalproject.R
import com.example.finalproject.domain.model.Song
import com.example.finalproject.ui.theme.WaveformBar
import com.example.finalproject.util.formatTime
import com.example.finalproject.util.getWaveForm
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    songList: List<Song>,
    initialIndex: Int = 0,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val playerState by viewModel.playerState.collectAsState()
    // Получаем ExoPlayer из PlayerManager через ViewModel
    val playerManager = viewModel.playerManager
    val exoPlayer = remember { playerManager.getPlayer() }
    val waveform = remember { getWaveForm() }

    // Создаем ключ на основе ID выбранной песни и индекса для гарантированного перезапуска
    val selectedSongId = songList.getOrNull(initialIndex)?.id ?: -1L
    val initKey = "$selectedSongId-$initialIndex"

    // Инициализация - перезапускается при каждом новом открытии Activity
    LaunchedEffect(key1 = initKey) {
        if (songList.isEmpty()) return@LaunchedEffect
        viewModel.initialize(songList, initialIndex)
        // PlayerManager уже играет песню через initialize
    }

    // Реагируем на изменение текущей песни (next/previous/shuffle)
    LaunchedEffect(key1 = playerState.currentIndex, key2 = playerState.isShuffle) {
        if (songList.isEmpty()) return@LaunchedEffect
        if (playerState.currentSong == null) return@LaunchedEffect
        
        val list = if (playerState.isShuffle) playerState.shuffledList else songList
        val song = list.getOrNull(playerState.currentIndex) ?: return@LaunchedEffect
        // PlayerManager уже обновлен через next()/previous() в ViewModel
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlay: Boolean) {
                viewModel.setPlaying(isPlay)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    viewModel.setDuration(exoPlayer.duration)
                }
                if (playbackState == Player.STATE_ENDED) {
                    viewModel.next()
                }
            }
        }
        playerManager.addListener(listener)
        onDispose {
            playerManager.removeListener(listener)
            // НЕ освобождаем player, так как он должен работать в фоне
        }
    }

    // Синхронизируем начальную позицию сразу после инициализации
    LaunchedEffect(key1 = initKey) {
        if (songList.isEmpty()) return@LaunchedEffect
        // Небольшая задержка для того, чтобы ExoPlayer успел загрузить метаданные
        delay(100)
        val currentPosition = exoPlayer.currentPosition
        val duration = exoPlayer.duration
        if (duration > 0 && currentPosition > 0) {
            viewModel.setElapsed(currentPosition)
            viewModel.setDuration(duration)
            viewModel.setWaveformProgress(currentPosition.toFloat() / duration)
        }
    }
    
    LaunchedEffect(playerState.isPlaying) {
        while (playerState.isPlaying) {
            val currentPosition = exoPlayer.currentPosition
            viewModel.setElapsed(currentPosition)
            val progress = if (playerState.duration > 0) {
                currentPosition.toFloat() / playerState.duration
            } else {
                0f
            }
            viewModel.setWaveformProgress(progress)
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xff191c1f),
                        Color(0xff2c2c38)
                    )
                )
            )
    ) {
        playerState.currentSong?.let { song ->
            val albumUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                song.albumId
            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(albumUri)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(18.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.40f,
                error = painterResource(R.drawable.baseline_music_note_24),
                placeholder = painterResource(R.drawable.baseline_music_note_24)
            )
            Row(Modifier.padding(horizontal = 16.dp, vertical = 48.dp)) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x30ffffff), shape = CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { 
                        viewModel.toggleFavorite()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0x30ffffff), shape = CircleShape)
                ) {
                    Icon(
                        if (playerState.currentSong?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (playerState.currentSong?.isFavorite == true) Color(0xFFFF6B6B) else Color.White
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        song.albumId
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(320.dp)
                        .clip(CircleShape)
                        .background(Color(0x30ffffff), shape = CircleShape),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.baseline_music_note_24),
                    placeholder = painterResource(R.drawable.baseline_music_note_24)
                )

                Text(
                    text = song.title.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 32.dp, end = 24.dp)
                )
                Text(
                    text = song.artist.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 48.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .padding(top = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WaveformBar(
                    values = waveform,
                    progress = playerState.waveformProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) { percent ->
                    val seek = (percent * playerState.duration).toLong()
                    exoPlayer.seekTo(seek)
                    viewModel.setElapsed(seek)
                    viewModel.setWaveformProgress(percent)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatTime((playerState.elapsed / 1000).toInt()),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        formatTime((playerState.duration / 1000).toInt()),
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 54.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val newRepeat = !playerState.isRepeat
                        viewModel.setRepeat(newRepeat)
                        exoPlayer.repeatMode = if (newRepeat) {
                            Player.REPEAT_MODE_ONE
                        } else {
                            Player.REPEAT_MODE_OFF
                        }
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.outline_repeat_one_24),
                        contentDescription = null,
                        tint = if (playerState.isRepeat) Color(0xff9c27b0) else Color.White
                    )
                }

                IconButton(onClick = { viewModel.previous() }) {
                    Icon(
                        painterResource(R.drawable.baseline_skip_previous_24),
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        if (exoPlayer.isPlaying) playerManager.pause() else playerManager.play()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White, shape = CircleShape)
                ) {
                    Icon(
                        painterResource(
                            if (playerState.isPlaying) R.drawable.baseline_pause_24
                            else R.drawable.baseline_play_arrow_24
                        ),
                        contentDescription = null,
                        tint = Color(0xff1a1a1a)
                    )
                }

                IconButton(onClick = { viewModel.next() }) {
                    Icon(
                        painterResource(R.drawable.baseline_skip_next_24),
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        val shuffled = if (!playerState.isShuffle) {
                            songList.shuffled()
                        } else {
                            songList
                        }
                        viewModel.setShuffle(!playerState.isShuffle, shuffled)
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_shuffle_24),
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

