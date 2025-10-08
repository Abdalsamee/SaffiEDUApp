package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.content.ContentValues.TAG
import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * مدير التقاط الصور من الكاميرا الأمامية
 */
class FrontCameraSnapshotManager(
    private val sessionManager: ExamSessionManager
) {
    private val TAG = "FrontSnapshotManager"
    private val randomCaptureRequested = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _snapshotStats = MutableStateFlow(SnapshotStats())
    val snapshotStats: StateFlow<SnapshotStats> = _snapshotStats.asStateFlow()

    private val lastSnapshotTime = mutableMapOf<SnapshotReason, Long>()
    private val MIN_SNAPSHOT_INTERVAL = 30_000L // 30 ثانية

    fun requestRandomCapture() {
        randomCaptureRequested.set(true)
    }



    /**
     * معالجة نتيجة Face Detection
     */
    fun processFaceDetectionResult(
        result: FaceDetectionResult,
        imageProxy: ImageProxy
    ) {
        // ← أضف هذا في أول الدالة processFaceDetectionResult
        if (randomCaptureRequested.compareAndSet(true, false)) {
            // التقاط لقطة عشوائية من نفس الفريم
            captureSnapshotSafely(imageProxy, SnapshotReason.PERIODIC_CHECK)
            return
        }


        if (!sessionManager.canCaptureMoreSnapshots()) {
            Log.w(TAG, "⚠️ Max snapshots reached")
            imageProxy.close()
            return
        }

        val shouldCapture = when (result) {
            is FaceDetectionResult.NoFace -> {
                shouldCaptureForReason(SnapshotReason.NO_FACE_DETECTED)
            }
            is FaceDetectionResult.MultipleFaces -> {
                true // أولوية عالية
            }
            is FaceDetectionResult.LookingAway -> {
                shouldCaptureForReason(SnapshotReason.LOOKING_AWAY)
            }
            else -> false
        }

        if (shouldCapture) {
            val reason = when (result) {
                is FaceDetectionResult.NoFace -> SnapshotReason.NO_FACE_DETECTED
                is FaceDetectionResult.MultipleFaces -> SnapshotReason.MULTIPLE_FACES
                is FaceDetectionResult.LookingAway -> SnapshotReason.LOOKING_AWAY
                else -> SnapshotReason.PERIODIC_CHECK
            }

            captureSnapshotSafely(imageProxy, reason)
        } else {
            imageProxy.close()
        }
    }

    /**
     * التقاط snapshot بشكل آمن
     */
    private fun captureSnapshotSafely(imageProxy: ImageProxy, reason: SnapshotReason) {
        try {
            Log.d(TAG, "📸 Capturing snapshot for: ${reason.name}")

            // نسخ البيانات فوراً
            val imageData = extractImageData(imageProxy)

            if (imageData != null) {
                lastSnapshotTime[reason] = System.currentTimeMillis()

                // حفظ في background thread
                scope.launch(Dispatchers.IO) {
                    val success = sessionManager.saveSnapshot(
                        imageData = imageData,
                        reason = reason
                    )

                    if (success) {
                        updateStats(reason, success = true)
                        Log.d(TAG, "✅ Snapshot saved: ${reason.name}")
                    } else {
                        updateStats(reason, success = false)
                        Log.w(TAG, "⚠️ Failed to save snapshot: ${reason.name}")
                    }
                }
            } else {
                Log.e(TAG, "❌ Failed to extract image data")
                updateStats(reason, success = false)
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error capturing snapshot", e)
            updateStats(reason, success = false)
        } finally {
            imageProxy.close()
        }
    }

    /**
     * استخراج بيانات الصورة
     */
    private fun extractImageData(imageProxy: ImageProxy): ImageData? {
        return try {
            if (imageProxy.format != ImageFormat.YUV_420_888) {
                Log.e(TAG, "Unsupported image format: ${imageProxy.format}")
                return null
            }

            val planes = imageProxy.planes
            if (planes.size < 3) {
                Log.e(TAG, "Invalid planes count: ${planes.size}")
                return null
            }

            // نسخ Y plane
            val yBuffer = planes[0].buffer
            val yData = ByteArray(yBuffer.remaining())
            yBuffer.get(yData)

            // نسخ U plane
            val uBuffer = planes[1].buffer
            val uData = ByteArray(uBuffer.remaining())
            uBuffer.get(uData)

            // نسخ V plane
            val vBuffer = planes[2].buffer
            val vData = ByteArray(vBuffer.remaining())
            vBuffer.get(vData)

            ImageData(
                format = imageProxy.format,
                width = imageProxy.width,
                height = imageProxy.height,
                yData = yData,
                uData = uData,
                vData = vData,
                yRowStride = planes[0].rowStride,
                yPixelStride = planes[0].pixelStride,
                uvRowStride = planes[1].rowStride,
                uvPixelStride = planes[1].pixelStride,
                vPixelStride = planes[2].pixelStride,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract ImageData", e)
            null
        }
    }

    /**
     * فحص إمكانية الالتقاط
     */
    private fun shouldCaptureForReason(reason: SnapshotReason): Boolean {
        if (reason == SnapshotReason.MULTIPLE_FACES) {
            return true
        }

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
     * التقاط Snapshot عشوائي (خارج نظام FaceDetection)
     */
    fun captureRandomSnapshot(reason: String = "RANDOM_SNAPSHOT") {
        try {
            Log.d(TAG, "📸 Triggering random snapshot for reason: $reason")

            // هذه الدالة لا تملك ImageProxy مباشر،
            // لذا سنقوم فقط بتسجيل حدث أو إحصائية مبدئيًا.
            // لاحقًا يمكن ربطها بـ ImageCapture عند تفعيل الكاميرا الأمامية.
            sessionManager.logSecurityEvent(
                type = SecurityEventType.SNAPSHOT_CAPTURED,
                details = "Random snapshot trigger invoked: $reason"
            )

            updateStats(SnapshotReason.PERIODIC_CHECK, success = true)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to capture random snapshot", e)
            updateStats(SnapshotReason.PERIODIC_CHECK, success = false)
        }
    }


    fun getRemainingSnapshotsCount(): Int {
        return sessionManager.getRemainingSnapshotsCount()
    }

    fun resetStats() {
        _snapshotStats.value = SnapshotStats()
        lastSnapshotTime.clear()
    }

    fun cleanup() {
        resetStats()
    }
}

/**
 * بيانات الصورة المنسوخة
 */
data class ImageData(
    val format: Int,
    val width: Int,
    val height: Int,
    val yData: ByteArray,
    val uData: ByteArray,
    val vData: ByteArray,
    val yRowStride: Int,
    val yPixelStride: Int,
    val uvRowStride: Int,
    val uvPixelStride: Int,
    val vPixelStride: Int,
    val rotationDegrees: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (format != other.format) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (!yData.contentEquals(other.yData)) return false
        if (!uData.contentEquals(other.uData)) return false
        if (!vData.contentEquals(other.vData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + yData.contentHashCode()
        result = 31 * result + uData.contentHashCode()
        result = 31 * result + vData.contentHashCode()
        return result
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

