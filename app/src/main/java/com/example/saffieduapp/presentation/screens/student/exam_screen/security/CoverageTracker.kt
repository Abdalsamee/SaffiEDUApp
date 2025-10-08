package com.example.saffieduapp.presentation.screens.student.exam_screen.security

class CoverageTracker {
    /**
     * يبني حالة واجهة المستخدم اعتماداً على الزمن المنقضي والهدف.
     * هنا نحاكي تقدّم yaw/pitch/roll بشكل بسيط متدرّج.
     */
    fun currentUiState(elapsedMs: Long, targetMs: Long): InExamScanUiState {
        val safeTarget = if (targetMs <= 0) 1L else targetMs
        val f = (elapsedMs.toFloat() / safeTarget).coerceIn(0f, 1f)

        val yaw = f                       // 100% مع نهاية الوقت
        val pitch = (f * 0.85f).coerceAtMost(1f)
        val roll = (f * 0.6f).coerceAtMost(1f)

        return InExamScanUiState(
            elapsedMs = elapsedMs.coerceAtLeast(0L),
            targetMs = safeTarget,
            yawProgress = yaw,
            pitchProgress = pitch,
            rollProgress = roll,
            allDirectionsCovered = f >= 1f
        )
    }
}
