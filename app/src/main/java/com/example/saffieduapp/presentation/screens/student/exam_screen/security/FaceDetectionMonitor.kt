package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FaceDetectionMonitor(
    private val onViolationDetected: (String) -> Unit,
    private val onSnapshotNeeded: ((ImageProxy, FaceDetectionResult) -> Unit)? = null
) {
    private val TAG = "FaceDetectionMonitor"

    private val faceDetector = FaceDetector()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _monitoringState = MutableStateFlow<MonitoringState>(MonitoringState.Idle)
    val monitoringState: StateFlow<MonitoringState> = _monitoringState.asStateFlow()

    private val _lastDetectionResult = MutableStateFlow<FaceDetectionResult?>(null)
    val lastDetectionResult: StateFlow<FaceDetectionResult?> = _lastDetectionResult.asStateFlow()

    @Volatile
    private var isMonitoring = false

    private var noFaceCount = 0
    private var lookingAwayCount = 0
    private var lastFaceDetectionTime = 0L

    private val MAX_NO_FACE_COUNT = 3
    private val MAX_LOOKING_AWAY_COUNT = 5
    private val DETECTION_INTERVAL = 2000L

    private var processingJob: Job? = null

    fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        noFaceCount = 0
        lookingAwayCount = 0
        lastFaceDetectionTime = System.currentTimeMillis()

        _monitoringState.value = MonitoringState.Active
        Log.d(TAG, "Face detection monitoring started")
    }

    fun stopMonitoring() {
        isMonitoring = false
        processingJob?.cancel()
        _monitoringState.value = MonitoringState.Stopped
        Log.d(TAG, "Face detection monitoring stopped")
    }

    @androidx.camera.core.ExperimentalGetImage
    fun processImage(imageProxy: ImageProxy) {
        if (!isMonitoring) {
            imageProxy.close()
            return
        }

        if (processingJob?.isActive == true) {
            imageProxy.close()
            return
        }

        val now = System.currentTimeMillis()
        if (now - lastFaceDetectionTime < DETECTION_INTERVAL) {
            imageProxy.close()
            return
        }

        lastFaceDetectionTime = now

        processingJob = scope.launch {
            try {
                _monitoringState.value = MonitoringState.Processing

                val result = faceDetector.detectFaces(imageProxy)

                // ✅ معالجة النتيجة مع ImageProxy مفتوح
                handleDetectionResult(result, imageProxy)

                _monitoringState.value = MonitoringState.Active

            } catch (e: Exception) {
                Log.e(TAG, "Error processing image", e)
                _monitoringState.value = MonitoringState.Error(e.message ?: "Unknown error")
                imageProxy.close()
            }
        }
    }

    private fun handleDetectionResult(result: FaceDetectionResult, imageProxy: ImageProxy) {
        _lastDetectionResult.value = result

        var needsSnapshot = false

        when (result) {
            is FaceDetectionResult.ValidFace -> {
                noFaceCount = 0
                lookingAwayCount = 0
                Log.d(TAG, "✅ Valid face detected")
                // ✅ إغلاق ImageProxy مباشرة لأننا لا نحتاج snapshot
                imageProxy.close()
            }

            is FaceDetectionResult.NoFace -> {
                noFaceCount++
                lookingAwayCount = 0

                Log.w(TAG, "⚠️ No face detected - Count: $noFaceCount")

                if (noFaceCount >= MAX_NO_FACE_COUNT) {
                    onViolationDetected("NO_FACE_DETECTED_LONG")
                    noFaceCount = 0
                }

                needsSnapshot = true
            }

            is FaceDetectionResult.MultipleFaces -> {
                Log.e(TAG, "🚨 Multiple faces detected: ${result.count}")
                onViolationDetected("MULTIPLE_FACES_DETECTED")
                noFaceCount = 0
                lookingAwayCount = 0
                needsSnapshot = true
            }

            is FaceDetectionResult.LookingAway -> {
                lookingAwayCount++
                noFaceCount = 0

                Log.w(TAG, "⚠️ Looking away - Count: $lookingAwayCount, Angle: ${result.angle}")

                if (lookingAwayCount >= MAX_LOOKING_AWAY_COUNT) {
                    onViolationDetected("LOOKING_AWAY")
                    lookingAwayCount = 0
                }

                needsSnapshot = true
            }

            is FaceDetectionResult.Error -> {
                Log.e(TAG, "Face detection error: ${result.message}")
                imageProxy.close()
            }
        }

        // ✅ إرسال ImageProxy للـ callback (لن يُغلق هنا)
        // الـ callback مسؤول عن إغلاقه
        if (needsSnapshot && onSnapshotNeeded != null) {
            onSnapshotNeeded.invoke(imageProxy, result)
        } else if (needsSnapshot) {
            // إذا لم يكن هناك callback، نغلق ImageProxy
            imageProxy.close()
        }
    }

    fun getStats(): MonitoringStats {
        return MonitoringStats(
            noFaceCount = noFaceCount,
            lookingAwayCount = lookingAwayCount,
            isActive = isMonitoring
        )
    }

    fun cleanup() {
        stopMonitoring()
        scope.cancel()
        faceDetector.cleanup()
        Log.d(TAG, "Face detection monitor cleaned up")
    }
}

sealed class MonitoringState {
    object Idle : MonitoringState()
    object Active : MonitoringState()
    object Processing : MonitoringState()
    object Stopped : MonitoringState()
    data class Error(val message: String) : MonitoringState()
}

data class MonitoringStats(
    val noFaceCount: Int,
    val lookingAwayCount: Int,
    val isActive: Boolean
)