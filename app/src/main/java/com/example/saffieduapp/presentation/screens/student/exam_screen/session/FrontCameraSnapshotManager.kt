package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.util.Log
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FrontCameraSnapshotManager(
    private val sessionManager: ExamSessionManager
) {
    private val TAG = "FrontSnapshotManager"

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _snapshotStats = MutableStateFlow(SnapshotStats())
    val snapshotStats: StateFlow<SnapshotStats> = _snapshotStats.asStateFlow()

    private val lastSnapshotTime = mutableMapOf<SnapshotReason, Long>()
    private val MIN_SNAPSHOT_INTERVAL = 30_000L

    fun processFaceDetectionResult(
        result: com.example.saffieduapp.presentation.screens.student.exam_screen.security.FaceDetectionResult,
        imageProxy: ImageProxy
    ) {
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
                true
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

            // ✅ نسخ البيانات فوراً قبل إغلاق ImageProxy
            val imageData = copyImageProxyData(imageProxy)
            imageProxy.close() // إغلاق فوري

            // معالجة في background
            scope.launch {
                captureSnapshot(imageData, reason)
            }
        } else {
            imageProxy.close()
        }
    }

    /**
     * ✅ نسخ بيانات ImageProxy قبل إغلاقه
     */
    private fun copyImageProxyData(imageProxy: ImageProxy): ImageData {
        return try {
            val yPlane = imageProxy.planes[0]
            val uPlane = imageProxy.planes[1]
            val vPlane = imageProxy.planes[2]

            ImageData(
                width = imageProxy.width,
                height = imageProxy.height,
                format = imageProxy.format,
                rotationDegrees = imageProxy.imageInfo.rotationDegrees,
                yData = copyBuffer(yPlane.buffer),
                uData = copyBuffer(uPlane.buffer),
                vData = copyBuffer(vPlane.buffer),
                yRowStride = yPlane.rowStride,
                uRowStride = uPlane.rowStride,
                vRowStride = vPlane.rowStride,
                yPixelStride = yPlane.pixelStride,
                uPixelStride = uPlane.pixelStride,
                vPixelStride = vPlane.pixelStride
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy ImageProxy data", e)
            throw e
        }
    }

    private fun copyBuffer(buffer: java.nio.ByteBuffer): ByteArray {
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        buffer.rewind()
        return data
    }

    private suspend fun captureSnapshot(imageData: ImageData, reason: SnapshotReason) {
        try {
            Log.d(TAG, "📸 Capturing snapshot for: ${reason.name}")

            val success = sessionManager.saveSnapshot(imageData, reason)

            if (success) {
                lastSnapshotTime[reason] = System.currentTimeMillis()
                updateStats(reason, success = true)
                Log.d(TAG, "✅ Snapshot captured successfully: ${reason.name}")
            } else {
                updateStats(reason, success = false)
                Log.w(TAG, "⚠️ Failed to capture snapshot: ${reason.name}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error capturing snapshot", e)
            updateStats(reason, success = false)
        }
    }

    fun captureManualSnapshot(imageProxy: ImageProxy) {
        if (!sessionManager.canCaptureMoreSnapshots()) {
            Log.w(TAG, "⚠️ Max snapshots reached")
            imageProxy.close()
            return
        }

        val imageData = copyImageProxyData(imageProxy)
        imageProxy.close()

        scope.launch {
            captureSnapshot(imageData, SnapshotReason.MANUAL_CAPTURE)
        }
    }

    fun capturePeriodicSnapshot(imageProxy: ImageProxy) {
        if (!sessionManager.canCaptureMoreSnapshots()) {
            imageProxy.close()
            return
        }

        if (shouldCaptureForReason(SnapshotReason.PERIODIC_CHECK)) {
            val imageData = copyImageProxyData(imageProxy)
            imageProxy.close()

            scope.launch {
                captureSnapshot(imageData, SnapshotReason.PERIODIC_CHECK)
            }
        } else {
            imageProxy.close()
        }
    }

    private fun shouldCaptureForReason(reason: SnapshotReason): Boolean {
        if (reason == SnapshotReason.MULTIPLE_FACES) {
            return true
        }

        val lastTime = lastSnapshotTime[reason] ?: 0L
        val timeSinceLastSnapshot = System.currentTimeMillis() - lastTime

        return timeSinceLastSnapshot >= MIN_SNAPSHOT_INTERVAL
    }

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
 * ✅ بيانات منسوخة من ImageProxy
 */
data class ImageData(
    val width: Int,
    val height: Int,
    val format: Int,
    val rotationDegrees: Int,
    val yData: ByteArray,
    val uData: ByteArray,
    val vData: ByteArray,
    val yRowStride: Int,
    val uRowStride: Int,
    val vRowStride: Int,
    val yPixelStride: Int,
    val uPixelStride: Int,
    val vPixelStride: Int
)

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