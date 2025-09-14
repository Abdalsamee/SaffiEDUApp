package com.example.saffieduapp.presentation.screens.student.video_player

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(VideoPlayerState())
    val state = _state.asStateFlow()

     val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build() // ✅ هنا التهيئة مباشرة

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        totalDuration = exoPlayer.duration.coerceAtLeast(0L),
                        videoDuration = formatDuration(exoPlayer.duration),
                        hasError = false,
                        errorMessage = null
                    )
                }
                Player.STATE_BUFFERING -> _state.value = _state.value.copy(isLoading = true)
                Player.STATE_ENDED -> _state.value = _state.value.copy(
                    isPlaying = false,
                    currentPosition = exoPlayer.duration
                )
                Player.STATE_IDLE -> _state.value = _state.value.copy(isLoading = false)
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = _state.value.copy(
                hasError = true,
                errorMessage = error.message ?: "Unknown error",
                isLoading = false
            )
        }
    }

    init {
        exoPlayer.addListener(playerListener) // ✅ الآن آمن

        viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(
                    currentPosition = exoPlayer.currentPosition
                )
                delay(250)
            }
        }

        // ✅ جلب Base64 من الـ SavedStateHandle
        val videoBase64 = savedStateHandle.get<String>("videoBase64")
        if (!videoBase64.isNullOrEmpty()) {
            loadVideoFromBase64(videoBase64)
        } else {
            _state.value = _state.value.copy(
                hasError = true,
                errorMessage = "لا يوجد فيديو للتشغيل",
                isLoading = false
            )
        }
    }


     fun loadVideoFromBase64(base64: String) {
        viewModelScope.launch {
            try {
                val file = saveBase64ToFile("lesson_video", base64, "mp4")
                val mediaItem = MediaItem.fromUri(file.toUri())
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
                _state.value = _state.value.copy(isLoading = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    hasError = true,
                    errorMessage = e.message ?: "فشل تشغيل الفيديو",
                    isLoading = false
                )
            }
        }
    }

    private suspend fun saveBase64ToFile(fileName: String, base64Data: String, extension: String): File {
        val file = File(context.filesDir, "$fileName.$extension")
        if (!file.exists()) {
            val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
            withContext(Dispatchers.IO) {
                file.outputStream().use { it.write(bytes) }
            }
        }
        return file
    }

    fun onFullscreenToggle() {
        _state.value = _state.value.copy(isFullscreen = !_state.value.isFullscreen)
    }

    fun playVideo() = exoPlayer.play()
    fun pauseVideo() = exoPlayer.pause()
    fun seekTo(position: Long) = exoPlayer.seekTo(position)

    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "00:00"
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        super.onCleared()
    }
    fun setError(message: String) {
        _state.value = _state.value.copy(
            hasError = true,
            errorMessage = message,
            isLoading = false
        )
    }
}
