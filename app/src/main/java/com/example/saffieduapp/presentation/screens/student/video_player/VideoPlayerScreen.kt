package com.example.saffieduapp.presentation.screens.student.video_player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.video_player.components.LessonInfoCard
import com.example.saffieduapp.presentation.screens.student.video_player.components.VideoPlayerComponent
import com.example.saffieduapp.ui.theme.AppBackground
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    navController: NavController,
    videoUrl: String?, // الآن نستقبل رابط الفيديو مباشرة
    onNavigateUp: () -> Unit,
    viewModel: VideoPlayerViewModel = hiltViewModel(),
    onFullscreenChange: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val state by viewModel.state.collectAsState()

    // تحميل الفيديو من رابط Storage
    LaunchedEffect(videoUrl) {
        if (!videoUrl.isNullOrEmpty()) {
            viewModel.loadVideo(videoUrl)
        } else {
            Toast.makeText(context,"لا يوجد فيديو للتشغيل", Toast.LENGTH_SHORT).show()
        }
    }
    LaunchedEffect(state.isFullscreen) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val window = activity.window
        val insetsController = WindowCompat.getInsetsController(window, view)
        onFullscreenChange?.invoke(state.isFullscreen)
        if (state.isFullscreen) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, true)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            val activity = context as? Activity ?: return@onDispose
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            val window = activity.window
            WindowCompat.setDecorFitsSystemWindows(window, true)
            WindowCompat.getInsetsController(window, view)
                .show(WindowInsetsCompat.Type.systemBars())
        }
    }

    if (state.isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VideoPlayerComponent(
                exoPlayer = viewModel.exoPlayer,
                isFullscreen = true,
                onFullscreenToggle = viewModel::onFullscreenToggle,
                modifier = Modifier.fillMaxSize()
            )
        }
        return
    }

    AnimatedVisibility(visible = !state.isFullscreen) {
        Scaffold(
            topBar = {
                CommonTopAppBar(
                    title = state.lessonTitle,
                    onNavigateUp = onNavigateUp
                )
            },
            containerColor = AppBackground
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VideoPlayerComponent(
                    exoPlayer = viewModel.exoPlayer,
                    isFullscreen = false,
                    onFullscreenToggle = viewModel::onFullscreenToggle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )

                LessonInfoCard(
                    title = state.lessonTitle,
                    teacherName = state.teacherName,
                    teacherImageUrl = state.teacherImageUrl,
                    duration = state.videoDuration,
                    publicationDate = state.publicationDate,
                    description = state.lessonDescription
                )
            }
        }
    }
}
