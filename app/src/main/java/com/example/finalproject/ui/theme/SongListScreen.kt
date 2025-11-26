package com.example.finalproject.ui.theme

import android.Manifest
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finalproject.R
import com.example.finalproject.data.Song
import com.example.finalproject.data.getSongs
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SongListScreen(
    onSongClick:(songs:List<Song>, position: Int) -> Unit
) {
    val context = LocalContext.current
    val songState = remember{ mutableStateOf<List<Song>>(emptyList()) }
    val permission = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission)
    LaunchedEffect(permissionState.status) {
        if(permissionState.status.isGranted) {
            songState.value = getSongs(context)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(R.drawable.bg),
            null,
             modifier =  Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
            )
        Column(Modifier.fillMaxSize()) {
            Text(
                text="Explorer Artist",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 44.dp, bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )
            if (!permissionState.status.isGranted) {
                Button(onClick = { permissionState.launchPermissionRequest()},
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Permission to Musics")
                }
            }

        }
    }
 }