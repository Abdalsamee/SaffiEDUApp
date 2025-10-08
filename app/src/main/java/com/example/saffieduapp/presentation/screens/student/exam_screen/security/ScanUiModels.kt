package com.example.saffieduapp.presentation.screens.student.exam_screen.security

/**
 * حالة تغطية المسح (تُحدّث من RoomScanCoverageTracker)
 */
data class CoverageState(
    val yawCoveragePercent: Float = 0f,    // 0..1
    val pitchCoveragePercent: Float = 0f,  // 0..1
    val pitchUpReached: Boolean = false,
    val pitchDownReached: Boolean = false
) {
    val yawComplete: Boolean get() = yawCoveragePercent >= 0.75f
    val pitchComplete: Boolean get() = pitchUpReached && pitchDownReached
    val totalPercent: Float get() = ((yawCoveragePercent + pitchCoveragePercent) / 2f).coerceIn(0f, 1f)
    val complete: Boolean get() = yawComplete && pitchComplete
}

/**
 * حالة واجهة المسح المفاجئ داخل الامتحان
 */
data class InExamScanUiState(
    val visible: Boolean,
    val durationMs: Long,
    val coverage: CoverageState
)
