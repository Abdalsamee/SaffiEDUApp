package com.example.saffieduapp.presentation.screens.student.video_player.components

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.saffieduapp.R
import androidx.compose.material3.Surface
import androidx.compose.ui.res.painterResource

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerComponent(
    exoPlayer: ExoPlayer,
    isFullscreen: Boolean,
    onFullscreenToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // إنشاء PlayerView
    val playerView = remember {
        PlayerView(context).apply {
            useController = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            setShutterBackgroundColor(android.graphics.Color.BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    DisposableEffect(playerView) {
        playerView.player = exoPlayer
        onDispose {
            playerView.player = null
        }
    }

    Box(modifier = modifier.background(Color.Black)) {
        // الفيديو نفسه
        AndroidView(
            factory = { playerView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.player = exoPlayer
                view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                view.controllerAutoShow = true
                view.controllerShowTimeoutMs = 3000
                view.controllerHideOnTouch = true
            }
        )

        // زر ملء الشاشة (Overlay فوق الفيديو)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                IconButton(onClick = { onFullscreenToggle() }) {
                    Icon(
                        painter = painterResource(
                            id = if (isFullscreen) R.drawable.plus else R.drawable.close
                        ),
                        contentDescription = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
