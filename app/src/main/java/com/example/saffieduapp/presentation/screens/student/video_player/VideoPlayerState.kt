package com.example.saffieduapp.presentation.screens.student.video_player

data class VideoPlayerState(
    val isLoading: Boolean = true,
    val videoUrl: String? = null,
    val lessonTitle: String = "",
    val lessonDescription: String = "",
    val teacherName: String = "",
    val teacherImageUrl: String = "",
    val videoDuration: String = "",
    val publicationDate: String = "",
    val isFullscreen: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)
