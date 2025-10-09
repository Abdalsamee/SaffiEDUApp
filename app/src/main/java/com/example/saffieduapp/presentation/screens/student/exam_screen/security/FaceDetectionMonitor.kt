package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.os.SystemClock
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
 * مراقب كشف الوجوه (إصدار بتهدئة زمنيّة)
 * - يعتمد على نوافذ زمنية بدلاً من عدّ الإطارات لتقليل الحساسية المفرطة
 * - يرسل رموز المخالفة لـ ExamSecurityManager
 * - يمرّر نتيجة الكشف و ImageProxy إلى FrontCameraSnapshotManager ليقرر التقاط snapshot من نفس الفريم
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

    // ───── ضبط الحساسية (يمكن تعديل القيم لاحقًا بسهولة) ─────
    private val NO_FACE_WINDOW_MS = 8_000L            // لا وجه لمدة ≥ 8 ثوانٍ
    private val MULTIPLE_FACES_WINDOW_MS = 3_000L     // أكثر من وجه لمدة ≥ 3 ثوانٍ
    private val LOOKING_AWAY_WINDOW_MS = 5_000L       // النظر بعيدًا لمدة ≥ 5 ثوانٍ
    private val HEAD_ROTATION_THRESHOLD = 35f         // زاوية الانحراف لاعتبار "ينظر بعيدًا"

    // طوابع زمنية لبدء الحالة (بدلاً من العدّ بالإطارات)
    private var noFaceStartAt: Long? = null
    private var multipleFacesStartAt: Long? = null
    private var lookingAwayStartAt: Long? = null

    // إحصاءات بسيطة
    private var totalNoFaceViolations = 0
    private var totalMultipleFacesViolations = 0

    @Volatile
    private var isMonitoring = false

    fun startMonitoring() {
        isMonitoring = true
        resetAllTimers()
        Log.d(TAG, "✅ Face detection monitoring started")
    }

    fun stopMonitoring() {
        isMonitoring = false
        Log.d(TAG, "❌ Face detection monitoring stopped")
    }

    private fun resetAllTimers() {
        noFaceStartAt = null
        multipleFacesStartAt = null
        lookingAwayStartAt = null
    }

    /**
     * معالجة كل فريم من الـ ImageAnalysis
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
                val now = SystemClock.elapsedRealtime()

                val result: FaceDetectionResult = when {
                    faces.isEmpty() -> {
                        // لا يوجد وجه
                        if (noFaceStartAt == null) noFaceStartAt = now
                        // أعد ضبط المؤقتات الأخرى
                        multipleFacesStartAt = null
                        lookingAwayStartAt = null

                        val elapsed = now - (noFaceStartAt ?: now)
                        Log.d(TAG, "⚠️ No face - ${elapsed}ms")

                        if (elapsed >= NO_FACE_WINDOW_MS) {
                            totalNoFaceViolations++
                            withContext(Dispatchers.Main) {
                                onViolationDetected("NO_FACE_DETECTED_LONG")
                            }
                            Log.w(TAG, "🚨 NO_FACE_DETECTED_LONG (total=$totalNoFaceViolations)")
                            // بعد تسجيل المخالفة، أعد فتح نافذة جديدة
                            noFaceStartAt = now
                        }

                        FaceDetectionResult.NoFace(elapsedMs = elapsed)
                    }

                    faces.size > 1 -> {
                        // أكثر من وجه
                        if (multipleFacesStartAt == null) multipleFacesStartAt = now
                        // أعد ضبط المؤقتات الأخرى
                        noFaceStartAt = null
                        lookingAwayStartAt = null

                        val elapsed = now - (multipleFacesStartAt ?: now)
                        Log.d(TAG, "⚠️ Multiple faces (${faces.size}) - ${elapsed}ms")

                        if (elapsed >= MULTIPLE_FACES_WINDOW_MS) {
                            totalMultipleFacesViolations++
                            withContext(Dispatchers.Main) {
                                onViolationDetected("MULTIPLE_FACES_DETECTED")
                            }
                            Log.w(TAG, "🚨 MULTIPLE_FACES_DETECTED (total=$totalMultipleFacesViolations)")
                            // افتح نافذة جديدة لو استمر الوضع
                            multipleFacesStartAt = now
                        }

                        FaceDetectionResult.MultipleFaces(faceCount = faces.size, elapsedMs = elapsed)
                    }

                    else -> {
                        // وجه واحد: افحص اتجاه الرأس
                        val face = faces[0]
                        val eulerY = face.headEulerAngleY
                        val eulerZ = face.headEulerAngleZ
                        val isLookingAway = abs(eulerY) > HEAD_ROTATION_THRESHOLD ||
                                abs(eulerZ) > HEAD_ROTATION_THRESHOLD

                        // بمجرد وجود وجه، نوقف عدّاد "لا وجه"
                        noFaceStartAt = null
                        // و”تعدد الوجوه“ بالتبعية
                        multipleFacesStartAt = null

                        if (isLookingAway) {
                            if (lookingAwayStartAt == null) lookingAwayStartAt = now
                            val elapsed = now - (lookingAwayStartAt ?: now)

                            if (elapsed >= LOOKING_AWAY_WINDOW_MS) {
                                Log.w(TAG, "⚠️ Looking away for too long ($elapsed ms)")
                                // لا نرفع مخالفة قوية هنا، فقط نتيح للسナップشوت أن يلتقط عند الاشتباه
                                // ونعيد فتح نافذة جديدة لو استمر الوضع
                                lookingAwayStartAt = now
                            }

                            FaceDetectionResult.LookingAway(eulerY = eulerY, eulerZ = eulerZ, elapsedMs = elapsed)
                        } else {
                            // عودة إلى حالة طبيعية → صفّر مؤقت النظر بعيدًا
                            lookingAwayStartAt = null
                            FaceDetectionResult.ValidFace(face)
                        }
                    }
                }

                _lastDetectionResult.value = result

                // مرّر الإطار ونتيجته إلى مدير الـ snapshots (هو سيغلق الـ imageProxy)
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

        Log.d(
            TAG, """
            📊 Final Statistics:
            - Total No-Face Violations: $totalNoFaceViolations
            - Total Multiple-Faces Violations: $totalMultipleFacesViolations
        """.trimIndent()
        )
    }
}

/** نتائج كشف الوجه (محدّثة بإضافة elapsedMs لحالات زمنية) */
sealed class FaceDetectionResult {
    data class ValidFace(val face: Face) : FaceDetectionResult()
    data class NoFace(val elapsedMs: Long) : FaceDetectionResult()
    data class MultipleFaces(val faceCount: Int, val elapsedMs: Long) : FaceDetectionResult()
    data class LookingAway(val eulerY: Float, val eulerZ: Float, val elapsedMs: Long) : FaceDetectionResult()
}
