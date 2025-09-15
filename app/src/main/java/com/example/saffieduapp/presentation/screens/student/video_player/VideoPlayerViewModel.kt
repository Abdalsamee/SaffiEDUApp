package com.example.saffieduapp.presentation.screens.student.video_player

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import java.util.*

@HiltViewModel
class VideoPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
    ) : ViewModel() {

    private val _state = MutableStateFlow(VideoPlayerState(errorMessage = "فشل تحميل الفيديو"))
    val state = _state.asStateFlow()

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        totalDuration = exoPlayer.duration.coerceAtLeast(0L),
                        videoDuration = formatDuration(exoPlayer.duration),
                        hasError = false
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
        exoPlayer.addListener(playerListener)
        viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(currentPosition = exoPlayer.currentPosition)
                delay(250)
            }
        }

        // جلب videoUrl و lessonId من SavedStateHandle
        val videoUrl = savedStateHandle.get<String>("videoUrl")
        val lessonId = savedStateHandle.get<String>("lessonId")

        if (!lessonId.isNullOrEmpty()) {
            loadLessonData(lessonId, videoUrl)
        } else {
            _state.value = _state.value.copy(
                hasError = true,
                errorMessage = "لا يوجد درس محدد",
                isLoading = false
            )
        }
    }
    private fun loadLessonData(lessonId: String, videoUrl: String?) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("lessons").document(lessonId).get().await()
                val description = doc.getString("description") ?: ""
                val publicationDate = doc.getString("publicationDate") ?: ""

                _state.value = _state.value.copy(
                    lessonDescription = description,
                    publicationDate = publicationDate
                )

                // تحميل الفيديو إذا كان موجود
                videoUrl?.let { loadVideo(it) }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    hasError = true,
                    errorMessage = "فشل جلب بيانات الدرس",
                    isLoading = false
                )
            }
        }
    }

    fun loadVideo(videoUrl: String) {
        viewModelScope.launch {
            try {
                val mediaItem = MediaItem.fromUri(videoUrl) // ← هنا نستخدم Media3
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    hasError = true,
                    errorMessage = "فشل تحميل الفيديو",
                    isLoading = false
                )
            }
        }
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
}
