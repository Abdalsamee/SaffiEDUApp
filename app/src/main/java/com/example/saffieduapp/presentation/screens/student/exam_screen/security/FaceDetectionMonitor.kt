package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlin.math.abs

/**
 * مراقب كشف الوجوه
 */
class FaceDetectionMonitor(
    private val onViolationDetected: (String) -> Unit,
    private val onSnapshotNeeded: (ImageProxy, FaceDetectionResult) -> Unit
) {
    private val TAG = "FaceDetectionMonitor"

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val detectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(0.15f)
        .build()

    private val faceDetector = FaceDetection.getClient(detectorOptions)

    private val _lastDetectionResult = MutableStateFlow<FaceDetectionResult?>(null)
    val lastDetectionResult: StateFlow<FaceDetectionResult?> = _lastDetectionResult.asStateFlow()

    private var consecutiveNoFaceCount = 0
    private var consecutiveLookingAwayCount = 0

    private val MAX_NO_FACE_THRESHOLD = 3
    private val MAX_LOOKING_AWAY_THRESHOLD = 5
    private val HEAD_ROTATION_THRESHOLD = 30f

    @Volatile
    private var isMonitoring = false

    fun startMonitoring() {
        isMonitoring = true
        consecutiveNoFaceCount = 0
        consecutiveLookingAwayCount = 0
        Log.d(TAG, "Face detection monitoring started")
    }

    fun stopMonitoring() {
        isMonitoring = false
        Log.d(TAG, "Face detection monitoring stopped")
    }

    /**
     * معالجة الصورة
     */
    @androidx.camera.core.ExperimentalGetImage
    fun processImage(imageProxy: ImageProxy) {
        if (!isMonitoring) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scope.launch {
            try {
                val faces: List<Face> = faceDetector.process(inputImage).await()

                val result = when {
                    faces.isEmpty() -> {
                        consecutiveNoFaceCount++
                        consecutiveLookingAwayCount = 0

                        if (consecutiveNoFaceCount >= MAX_NO_FACE_THRESHOLD) {
                            withContext(Dispatchers.Main) {
                                onViolationDetected("لم يتم اكتشاف وجه")
                            }
                        }

                        FaceDetectionResult.NoFace(consecutiveNoFaceCount)
                    }

                    faces.size > 1 -> {
                        consecutiveNoFaceCount = 0
                        consecutiveLookingAwayCount = 0

                        withContext(Dispatchers.Main) {
                            onViolationDetected("تم اكتشاف أكثر من وجه")
                        }

                        FaceDetectionResult.MultipleFaces(faces.size)
                    }

                    else -> {
                        val face = faces[0]
                        val rotY = face.headEulerAngleY

                        if (abs(rotY) > HEAD_ROTATION_THRESHOLD) {
                            consecutiveLookingAwayCount++
                            consecutiveNoFaceCount = 0

                            if (consecutiveLookingAwayCount >= MAX_LOOKING_AWAY_THRESHOLD) {
                                withContext(Dispatchers.Main) {
                                    onViolationDetected("الطالب ينظر بعيداً")
                                }
                            }

                            FaceDetectionResult.LookingAway(rotY, consecutiveLookingAwayCount)
                        } else {
                            consecutiveNoFaceCount = 0
                            consecutiveLookingAwayCount = 0
                            FaceDetectionResult.ValidFace
                        }
                    }
                }

                _lastDetectionResult.value = result
                handleDetectionResult(result, imageProxy)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing image", e)
                _lastDetectionResult.value = FaceDetectionResult.Error(e.message ?: "Unknown error")
                imageProxy.close()
            }
        }
    }

    /**
     * معالجة النتيجة
     */
    private fun handleDetectionResult(result: FaceDetectionResult, imageProxy: ImageProxy) {
        when (result) {
            is FaceDetectionResult.NoFace,
            is FaceDetectionResult.MultipleFaces,
            is FaceDetectionResult.LookingAway -> {
                onSnapshotNeeded(imageProxy, result)
            }
            else -> {
                imageProxy.close()
            }
        }
    }

    fun cleanup() {
        isMonitoring = false
        scope.cancel()
        faceDetector.close()
        Log.d(TAG, "Face detection monitor cleaned up")
    }
}

/**
 * نتائج كشف الوجه
 */
sealed class FaceDetectionResult {
    object ValidFace : FaceDetectionResult()
    data class NoFace(val count: Int) : FaceDetectionResult()
    data class MultipleFaces(val count: Int) : FaceDetectionResult()
    data class LookingAway(val angle: Float, val count: Int) : FaceDetectionResult()
    data class Error(val message: String) : FaceDetectionResult()
}