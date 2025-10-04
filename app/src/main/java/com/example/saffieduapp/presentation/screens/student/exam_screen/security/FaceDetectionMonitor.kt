package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مراقب كشف الوجوه - يدير عملية المراقبة المستمرة
 */
class FaceDetectionMonitor(
    private val onViolationDetected: (String) -> Unit
) {
    private val TAG = "FaceDetectionMonitor"

    private val faceDetector = FaceDetector()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _monitoringState = MutableStateFlow<MonitoringState>(MonitoringState.Idle)
    val monitoringState: StateFlow<MonitoringState> = _monitoringState.asStateFlow()

    @Volatile
    private var isMonitoring = false

    // إحصائيات المراقبة
    private var noFaceCount = 0
    private var lookingAwayCount = 0
    private var lastFaceDetectionTime = 0L

    // الحدود للكشف
    private val MAX_NO_FACE_COUNT = 3 // بعد 3 محاولات متتالية
    private val MAX_LOOKING_AWAY_COUNT = 5 // بعد 5 محاولات متتالية
    private val DETECTION_INTERVAL = 2000L // كل 2 ثانية

    private var processingJob: Job? = null

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        noFaceCount = 0
        lookingAwayCount = 0
        lastFaceDetectionTime = System.currentTimeMillis()

        _monitoringState.value = MonitoringState.Active
        Log.d(TAG, "Face detection monitoring started")
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        isMonitoring = false
        processingJob?.cancel()
        _monitoringState.value = MonitoringState.Stopped
        Log.d(TAG, "Face detection monitoring stopped")
    }

    /**
     * معالجة صورة من الكاميرا
     */
    @androidx.camera.core.ExperimentalGetImage
    fun processImage(imageProxy: ImageProxy) {
        if (!isMonitoring) {
            imageProxy.close()
            return
        }

        // تجنب المعالجة المتزامنة
        if (processingJob?.isActive == true) {
            imageProxy.close()
            return
        }

        // التحقق من الفاصل الزمني
        val now = System.currentTimeMillis()
        if (now - lastFaceDetectionTime < DETECTION_INTERVAL) {
            imageProxy.close()
            return
        }

        lastFaceDetectionTime = now

        // معالجة الصورة
        processingJob = scope.launch {
            try {
                _monitoringState.value = MonitoringState.Processing

                val result = faceDetector.detectFaces(imageProxy)
                handleDetectionResult(result)

                _monitoringState.value = MonitoringState.Active

            } catch (e: Exception) {
                Log.e(TAG, "Error processing image", e)
                _monitoringState.value = MonitoringState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * معالجة نتيجة الكشف
     */
    private fun handleDetectionResult(result: FaceDetectionResult) {
        when (result) {
            is FaceDetectionResult.ValidFace -> {
                // وجه صحيح - إعادة تعيين العدادات
                noFaceCount = 0
                lookingAwayCount = 0
                Log.d(TAG, "✅ Valid face detected")
            }

            is FaceDetectionResult.NoFace -> {
                noFaceCount++
                lookingAwayCount = 0 // إعادة تعيين

                Log.w(TAG, "⚠️ No face detected - Count: $noFaceCount")

                if (noFaceCount >= MAX_NO_FACE_COUNT) {
                    onViolationDetected("NO_FACE_DETECTED_LONG")
                    noFaceCount = 0 // إعادة تعيين بعد الإبلاغ
                }
            }

            is FaceDetectionResult.MultipleFaces -> {
                Log.e(TAG, "🚨 Multiple faces detected: ${result.count}")
                onViolationDetected("MULTIPLE_FACES_DETECTED")
                noFaceCount = 0
                lookingAwayCount = 0
            }

            is FaceDetectionResult.LookingAway -> {
                lookingAwayCount++
                noFaceCount = 0 // إعادة تعيين

                Log.w(TAG, "⚠️ Looking away - Count: $lookingAwayCount, Angle: ${result.angle}")

                if (lookingAwayCount >= MAX_LOOKING_AWAY_COUNT) {
                    onViolationDetected("LOOKING_AWAY")
                    lookingAwayCount = 0 // إعادة تعيين بعد الإبلاغ
                }
            }

            is FaceDetectionResult.Error -> {
                Log.e(TAG, "Face detection error: ${result.message}")
            }
        }
    }

    /**
     * الحصول على إحصائيات المراقبة
     */
    fun getStats(): MonitoringStats {
        return MonitoringStats(
            noFaceCount = noFaceCount,
            lookingAwayCount = lookingAwayCount,
            isActive = isMonitoring
        )
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        stopMonitoring()
        scope.cancel()
        faceDetector.cleanup()
        Log.d(TAG, "Face detection monitor cleaned up")
    }
}

/**
 * حالة المراقبة
 */
sealed class MonitoringState {
    object Idle : MonitoringState()
    object Active : MonitoringState()
    object Processing : MonitoringState()
    object Stopped : MonitoringState()
    data class Error(val message: String) : MonitoringState()
}

/**
 * إحصائيات المراقبة
 */
data class MonitoringStats(
    val noFaceCount: Int,
    val lookingAwayCount: Int,
    val isActive: Boolean
)