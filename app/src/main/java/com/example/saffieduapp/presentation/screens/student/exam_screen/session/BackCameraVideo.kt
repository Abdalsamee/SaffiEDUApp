package com.example.saffieduapp.presentation.screens.student.exam_screen.session

/**
 * بيانات فيديو الكاميرا الخلفية (مسح الغرفة)
 */
data class BackCameraVideo(
    val path: String,
    val timestamp: Long,
    val duration: Long,
    val fileSize: Long
)