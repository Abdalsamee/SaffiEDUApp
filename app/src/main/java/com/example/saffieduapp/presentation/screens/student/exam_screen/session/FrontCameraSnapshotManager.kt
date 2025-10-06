package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.graphics.ImageFormat
import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * مدير التقاط الصور الذكي من الكاميرا الأمامية
 * ✅ محدّث: يحل مشكلة "Image is already closed"
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
     * ✅ الحل: نسخ البيانات من ImageProxy قبل أي معالجة
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
                shouldCaptureForReason(SnapshotReason.NO_FACE_DETECTED)
            }
            is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.MultipleFaces -> {
                true // أولوية عالية
            }
            is com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult.LookingAway -> {
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

            // ✅ الحل: نسخ البيانات فوراً قبل إغلاق ImageProxy
            captureSnapshotSafely(imageProxy, reason)
        } else {
            imageProxy.close()
        }
    }

    /**
     * التقاط snapshot بطريقة آمنة - ينسخ البيانات قبل الإغلاق
     * ✅ الحل: نسخ البيانات من ImageProxy في Thread الحالي قبل إغلاقه
     */
    private fun captureSnapshotSafely(imageProxy: ImageProxy, reason: SnapshotReason) {
        try {
            Log.d(TAG, "📸 Capturing snapshot for: ${reason.name}")

            // ✅ نسخ ImageData كاملة فوراً قبل أي معالجة
            val imageData = extractImageData(imageProxy)

            if (imageData != null) {
                // تحديث وقت آخر snapshot
                lastSnapshotTime[reason] = System.currentTimeMillis()

                // معالجة وحفظ الصورة في background thread
                scope.launch(Dispatchers.IO) {
                    val success = sessionManager.saveSnapshot(
                        imageData = imageData,
                        reason = reason
                    )

                    if (success) {
                        updateStats(reason, success = true)
                        Log.d(TAG, "✅ Snapshot saved successfully: ${reason.name}")
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
            // ✅ إغلاق ImageProxy بعد نسخ البيانات
            imageProxy.close()
        }
    }

    /**
     * استخراج ImageData من ImageProxy بأمان
     * ✅ الحل: نسخ جميع البيانات المطلوبة قبل إغلاق ImageProxy
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

            // Y plane
            val yBuffer = planes[0].buffer
            val yData = ByteArray(yBuffer.remaining())
            yBuffer.get(yData)

            // U plane
            val uBuffer = planes[1].buffer
            val uData = ByteArray(uBuffer.remaining())
            uBuffer.get(uData)

            // V plane
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
     * فحص إمكانية الالتقاط لسبب معين
     */
    private fun shouldCaptureForReason(reason: SnapshotReason): Boolean {
        // Multiple faces دائماً يتم التقاطها
        if (reason == SnapshotReason.MULTIPLE_FACES) {
            return true
        }

        // التحقق من الوقت المنقضي
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
 * بيانات الصورة المنسوخة من ImageProxy
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