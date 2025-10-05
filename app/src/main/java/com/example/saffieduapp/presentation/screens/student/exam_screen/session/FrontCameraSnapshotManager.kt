package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مدير التقاط الصور الذكي من الكاميرا الأمامية
 * يلتقط تلقائياً عند اكتشاف مخالفات
 */
class FrontCameraSnapshotManager(
    private val sessionManager: ExamSessionManager
) {
    private val TAG = "FrontSnapshotManager"

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // إحصائيات الـ Snapshots
    private val _snapshotStats = MutableStateFlow(SnapshotStats())
    val snapshotStats: StateFlow<SnapshotStats> = _snapshotStats.asStateFlow()

    // تتبع آخر snapshot لكل سبب (لمنع التكرار السريع)
    private val lastSnapshotTime = mutableMapOf<SnapshotReason, Long>()

    // الحد الأدنى للوقت بين snapshots من نفس النوع (30 ثانية)
    private val MIN_SNAPSHOT_INTERVAL = 30_000L

    /**
     * معالجة نتيجة Face Detection والتقاط snapshot إذا لزم الأمر
     */
    fun processFaceDetectionResult(
        result: com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult,
        imageProxy: ImageProxy
    ) {
        // فحص إمكانية الالتقاط
        if (!sessionManager.canCaptureMoreSnapshots()) {
            Log.w(TAG, "⚠️ Max snapshots reached, skipping capture")
            imageProxy.close()
            return
        }

        val shouldCapture = when (result) {
            is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.NoFace -> {
                // التقاط عند عدم وجود وجه
                shouldCaptureForReason(SnapshotReason.NO_FACE_DETECTED)
            }

            is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.MultipleFaces -> {
                // التقاط عند وجود عدة وجوه (أولوية عالية)
                true
            }

            is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.LookingAway -> {
                // التقاط عند النظر بعيداً
                shouldCaptureForReason(SnapshotReason.LOOKING_AWAY)
            }

            else -> false
        }

        if (shouldCapture) {
            val reason = when (result) {
                is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.NoFace ->
                    SnapshotReason.NO_FACE_DETECTED
                is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.MultipleFaces ->
                    SnapshotReason.MULTIPLE_FACES
                is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.LookingAway ->
                    SnapshotReason.LOOKING_AWAY
                else -> SnapshotReason.PERIODIC_CHECK
            }

            captureSnapshot(imageProxy, reason)
        } else {
            imageProxy.close()
        }
    }

    /**
     * التقاط snapshot يدوياً
     */
    fun captureManualSnapshot(imageProxy: ImageProxy) {
        if (!sessionManager.canCaptureMoreSnapshots()) {
            Log.w(TAG, "⚠️ Max snapshots reached")
            imageProxy.close()
            return
        }

        captureSnapshot(imageProxy, SnapshotReason.MANUAL_CAPTURE)
    }

    /**
     * التقاط snapshot دوري
     */
    fun capturePeriodicSnapshot(imageProxy: ImageProxy) {
        if (!sessionManager.canCaptureMoreSnapshots()) {
            imageProxy.close()
            return
        }

        if (shouldCaptureForReason(SnapshotReason.PERIODIC_CHECK)) {
            captureSnapshot(imageProxy, SnapshotReason.PERIODIC_CHECK)
        } else {
            imageProxy.close()
        }
    }

    /**
     * التقاط snapshot
     */
    private fun captureSnapshot(imageProxy: ImageProxy, reason: SnapshotReason) {
        try {
            Log.d(TAG, "📸 Capturing snapshot for: ${reason.name}")

            val success = sessionManager.saveSnapshot(imageProxy, reason)

            if (success) {
                // تحديث وقت آخر snapshot
                lastSnapshotTime[reason] = System.currentTimeMillis()

                // تحديث الإحصائيات
                updateStats(reason, success = true)

                Log.d(TAG, "✅ Snapshot captured successfully: ${reason.name}")
            } else {
                updateStats(reason, success = false)
                Log.w(TAG, "⚠️ Failed to capture snapshot: ${reason.name}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error capturing snapshot", e)
            updateStats(reason, success = false)
        } finally {
            imageProxy.close()
        }
    }

    /**
     * فحص إمكانية الالتقاط لسبب معين
     */
    private fun shouldCaptureForReason(reason: SnapshotReason): Boolean {
        // Multiple faces دائماً يتم التقاطها (أولوية عالية)
        if (reason == SnapshotReason.MULTIPLE_FACES) {
            return true
        }

        // التحقق من الوقت المنقضي منذ آخر snapshot من نفس النوع
        val lastTime = lastSnapshotTime[reason] ?: 0L
        val timeSinceLastSnapshot = System.currentTimeMillis() - lastTime

        return timeSinceLastSnapshot >= MIN_SNAPSHOT_INTERVAL
    }

    /**
     * تحديث الإحصائيات
     */
    private fun updateStats(reason: SnapshotReason, success: Boolean) {
        val current = _snapshotStats.value

        _snapshotStats.value = when (reason) {
            SnapshotReason.NO_FACE_DETECTED -> current.copy(
                noFaceSnapshots = current.noFaceSnapshots + if (success) 1 else 0,
                totalAttempts = current.totalAttempts + 1,
                failedAttempts = current.failedAttempts + if (!success) 1 else 0
            )
            SnapshotReason.MULTIPLE_FACES -> current.copy(
                multipleFacesSnapshots = current.multipleFacesSnapshots + if (success) 1 else 0,
                totalAttempts = current.totalAttempts + 1,
                failedAttempts = current.failedAttempts + if (!success) 1 else 0
            )
            SnapshotReason.LOOKING_AWAY -> current.copy(
                lookingAwaySnapshots = current.lookingAwaySnapshots + if (success) 1 else 0,
                totalAttempts = current.totalAttempts + 1,
                failedAttempts = current.failedAttempts + if (!success) 1 else 0
            )
            SnapshotReason.MANUAL_CAPTURE -> current.copy(
                manualSnapshots = current.manualSnapshots + if (success) 1 else 0,
                totalAttempts = current.totalAttempts + 1,
                failedAttempts = current.failedAttempts + if (!success) 1 else 0
            )
            SnapshotReason.PERIODIC_CHECK -> current.copy(
                periodicSnapshots = current.periodicSnapshots + if (success) 1 else 0,
                totalAttempts = current.totalAttempts + 1,
                failedAttempts = current.failedAttempts + if (!success) 1 else 0
            )
        }
    }

    /**
     * الحصول على عدد الـ snapshots المتبقية
     */
    fun getRemainingSnapshotsCount(): Int {
        return sessionManager.getRemainingSnapshotsCount()
    }

    /**
     * إعادة تعيين الإحصائيات
     */
    fun resetStats() {
        _snapshotStats.value = SnapshotStats()
        lastSnapshotTime.clear()
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        resetStats()
    }
}

/**
 * إحصائيات الـ Snapshots
 */
data class SnapshotStats(
    val noFaceSnapshots: Int = 0,
    val multipleFacesSnapshots: Int = 0,
    val lookingAwaySnapshots: Int = 0,
    val manualSnapshots: Int = 0,
    val periodicSnapshots: Int = 0,
    val totalAttempts: Int = 0,
    val failedAttempts: Int = 0
) {
    val totalSuccessful: Int
        get() = noFaceSnapshots + multipleFacesSnapshots + lookingAwaySnapshots +
                manualSnapshots + periodicSnapshots

    val successRate: Float
        get() = if (totalAttempts > 0) {
            (totalSuccessful.toFloat() / totalAttempts) * 100
        } else {
            0f
        }
}