package com.example.saffieduapp.presentation.screens.student.exam_screen.security

/**
 * حالة واجهة المسح المفاجئ داخل الامتحان
 * (تُعرض في الـ Overlay)
 */
data class InExamScanUiState(
    val elapsedMs: Long,
    val targetMs: Long,
    val yawProgress: Float,    // تقدّم الدوران الأفقي 0..1
    val pitchProgress: Float,  // تقدّم الإمالة للأعلى/الأسفل 0..1
    val rollProgress: Float,   // تدوير حول محور Z 0..1
    val allDirectionsCovered: Boolean
)
