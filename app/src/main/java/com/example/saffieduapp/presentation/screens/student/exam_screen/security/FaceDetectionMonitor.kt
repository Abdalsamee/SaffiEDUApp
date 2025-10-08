package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.camera.core.ImageProxy
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
 * ✅ مُصلح: إرسال رموز بدلاً من نصوص
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
    private var consecutiveMultipleFacesCount = 0

    // ✅ عدادات التحذيرات الإجمالية
    private var totalNoFaceViolations = 0
    private var totalMultipleFacesViolations = 0

    // ✅ Thresholds محسّنة
    private val MAX_NO_FACE_THRESHOLD = 10  // 10 إطارات متتالية (~3 ثواني)
    private val MAX_MULTIPLE_FACES_THRESHOLD = 5  // 5 إطارات متتالية
    private val MAX_LOOKING_AWAY_THRESHOLD = 15
    private val HEAD_ROTATION_THRESHOLD = 35f

    @Volatile
    private var isMonitoring = false

    fun startMonitoring() {
        isMonitoring = true
        consecutiveNoFaceCount = 0
        consecutiveLookingAwayCount = 0
        consecutiveMultipleFacesCount = 0
        Log.d(TAG, "✅ Face detection monitoring started")
    }

    fun stopMonitoring() {
        isMonitoring = false
        Log.d(TAG, "❌ Face detection monitoring stopped")
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
                        consecutiveMultipleFacesCount = 0

                        Log.d(TAG, "⚠️ No face detected (count: $consecutiveNoFaceCount)")

                        if (consecutiveNoFaceCount >= MAX_NO_FACE_THRESHOLD) {
                            totalNoFaceViolations++

                            withContext(Dispatchers.Main) {
                                // ✅ إرسال رمز بدلاً من نص
                                onViolationDetected("NO_FACE_DETECTED_LONG")
                            }

                            Log.w(TAG, "🚨 NO_FACE_DETECTED_LONG violation triggered! (total: $totalNoFaceViolations)")

                            // ✅ إعادة تعيين العداد بعد التسجيل
                            consecutiveNoFaceCount = 0
                        }

                        FaceDetectionResult.NoFace(consecutiveNoFaceCount)
                    }

                    faces.size > 1 -> {
                        consecutiveNoFaceCount = 0
                        consecutiveLookingAwayCount = 0
                        consecutiveMultipleFacesCount++

                        Log.d(TAG, "⚠️ Multiple faces detected: ${faces.size} (count: $consecutiveMultipleFacesCount)")

                        if (consecutiveMultipleFacesCount >= MAX_MULTIPLE_FACES_THRESHOLD) {
                            totalMultipleFacesViolations++

                            withContext(Dispatchers.Main) {
                                // ✅ إرسال رمز بدلاً من نص
                                onViolationDetected("MULTIPLE_FACES_DETECTED")
                            }

                            Log.w(TAG, "🚨 MULTIPLE_FACES_DETECTED violation triggered! (total: $totalMultipleFacesViolations)")

                            // ✅ إعادة تعيين العداد بعد التسجيل
                            consecutiveMultipleFacesCount = 0
                        }

                        FaceDetectionResult.MultipleFaces(faces.size)
                    }

                    else -> {
                        val face = faces[0]

                        consecutiveNoFaceCount = 0
                        consecutiveMultipleFacesCount = 0

                        // فحص اتجاه الرأس
                        val headEulerAngleY = face.headEulerAngleY
                        val headEulerAngleZ = face.headEulerAngleZ

                        val isLookingAway = abs(headEulerAngleY) > HEAD_ROTATION_THRESHOLD ||
                                abs(headEulerAngleZ) > HEAD_ROTATION_THRESHOLD

                        if (isLookingAway) {
                            consecutiveLookingAwayCount++

                            if (consecutiveLookingAwayCount >= MAX_LOOKING_AWAY_THRESHOLD) {
                                Log.w(TAG, "⚠️ Looking away for too long")
                                consecutiveLookingAwayCount = 0
                            }

                            FaceDetectionResult.LookingAway(headEulerAngleY, headEulerAngleZ)
                        } else {
                            consecutiveLookingAwayCount = 0
                            FaceDetectionResult.ValidFace(face)
                        }
                    }
                }

                _lastDetectionResult.value = result

                // ✅ إرسال للـ Snapshot Manager
                withContext(Dispatchers.Main) {
                    onSnapshotNeeded(imageProxy, result)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing face detection", e)
                imageProxy.close()
            }
        }
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        stopMonitoring()
        scope.cancel()
        faceDetector.close()

        Log.d(TAG, """
            📊 Final Statistics:
            - Total No Face Violations: $totalNoFaceViolations
            - Total Multiple Faces Violations: $totalMultipleFacesViolations
        """.trimIndent())
    }
}

/**
 * نتائج كشف الوجه
 */
sealed class FaceDetectionResult {
    data class ValidFace(val face: Face) : FaceDetectionResult()
    data class NoFace(val consecutiveCount: Int) : FaceDetectionResult()
    data class MultipleFaces(val faceCount: Int) : FaceDetectionResult()
    data class LookingAway(val eulerY: Float, val eulerZ: Float) : FaceDetectionResult()
}