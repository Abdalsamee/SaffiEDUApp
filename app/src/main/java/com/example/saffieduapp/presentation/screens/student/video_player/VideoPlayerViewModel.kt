package com.example.saffieduapp.presentation.screens.student.video_player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        totalDuration = exoPlayer.duration.coerceAtLeast(0L),
                        hasError = false,
                        errorMessage = null,
                        videoDuration = formatDuration(exoPlayer.duration)
                    )
                }
                Player.STATE_BUFFERING -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
                Player.STATE_ENDED -> {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        currentPosition = exoPlayer.duration
                    )
                }
                Player.STATE_IDLE -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onPlayerError(error: PlaybackException) {
            _state.value = _state.value.copy(
                hasError = true,
                errorMessage = error.message ?: "Unknown error occurred",
                isLoading = false
            )
        }
    }

    init {
        exoPlayer.addListener(playerListener)

        // تحديث موضع التشغيل بشكل دوري سواء كان Playing أم لا (لإظهار الشريط بدقة)
        viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(
                    currentPosition = exoPlayer.currentPosition
                )
                delay(250)
            }
        }

        val videoId = savedStateHandle.get<String>("videoId")
        if (videoId != null) {
            loadVideoData(videoId)
        } else {
            loadVideoData("sample")
        }
    }

    private fun loadVideoData(id: String) {
        val sampleVideoUrl =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

        try {
            val mediaItem = MediaItem.fromUri(sampleVideoUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            _state.value = _state.value.copy(
                isLoading = true,
                videoUrl = sampleVideoUrl,
                lessonTitle = "قولو لعين الشمس",
                lessonDescription = "هذا فيديو تعليمي يشرح كيفية استخدام التطبيق بطريقة سهلة ومبسطة",
                teacherName = "المعلم أحمد محمد",
                teacherImageUrl = "",
                videoDuration = "--:--", // يتم تحديثه عند READY
                publicationDate = getCurrentDate(),
                hasError = false,
                errorMessage = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                hasError = true,
                errorMessage = e.message ?: "Error loading video",
                isLoading = false
            )
        }
    }

    fun onFullscreenToggle() {
        _state.value = _state.value.copy(isFullscreen = !_state.value.isFullscreen)
        // لا حاجة لتأخيرات هنا؛ PlayerView يتكيف فورًا مع تعديل الحجم
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

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    override fun onCleared() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
        super.onCleared()
    }
}
