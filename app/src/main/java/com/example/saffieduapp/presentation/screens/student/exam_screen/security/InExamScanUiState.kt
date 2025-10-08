package com.example.saffieduapp.presentation.screens.student.exam_screen.security

/**
 * حالة تغطية المسح (تتحدث من RoomScanCoverageTracker)
 */
data class CoverageState(
    val yawCoveragePercent: Float = 0f,    // تغطية أفقية 0..1
    val pitchCoveragePercent: Float = 0f,  // تغطية رأسية 0..1
    val pitchUpReached: Boolean = false,   // تم رفع الهاتف لأعلى بدرجة كافية
    val pitchDownReached: Boolean = false  // تم خفض الهاتف لأسفل بدرجة كافية
) {
    val yawComplete: Boolean get() = yawCoveragePercent >= 0.75f      // افتراضي: 75% كفاية
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
