package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.FrontCameraSnapshotManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مراقب الكاميرا الشامل - يدمج الكاميرا مع Face Detection و Snapshots
 * ✅ محدّث: يتضمن ExamSessionManager و SnapshotManager
 */
class CameraMonitor(
    private val context: Context,
    private val onViolationDetected: (String) -> Unit,
    private val sessionManager: ExamSessionManager
) {
    private val TAG = "CameraMonitor"

    private val cameraManager = CameraManager(context)

    // ✅ SnapshotManager
    private val snapshotManager = FrontCameraSnapshotManager(sessionManager)

    // ✅ FaceDetectionMonitor مع callback للـ snapshots
    private val faceDetectionMonitor = FaceDetectionMonitor(
        onViolationDetected = onViolationDetected,
        onSnapshotNeeded = { imageProxy, result ->
            // معالجة الصورة للـ snapshot
            snapshotManager.processFaceDetectionResult(result, imageProxy)
        }
    )

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isFrontCameraActive = MutableStateFlow(false)
    val isFrontCameraActive: StateFlow<Boolean> = _isFrontCameraActive.asStateFlow()

    private val _isBackCameraActive = MutableStateFlow(false)
    val isBackCameraActive: StateFlow<Boolean> = _isBackCameraActive.asStateFlow()

    @Volatile
    private var isMonitoring = false

    /**
     * تهيئة نظام الكاميرا
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            Log.d(TAG, "Initializing camera system...")
            cameraManager.initialize()
            _isInitialized.value = true
            Log.d(TAG, "Camera system initialized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize camera system", e)
            Result.failure(e)
        }
    }

    /**
     * بدء المراقبة الكاملة
     */
    @OptIn(ExperimentalGetImage::class)
    fun startMonitoring(
        lifecycleOwner: LifecycleOwner,
        frontPreviewView: PreviewView? = null
    ) {
        Log.d(TAG, "🔹 startMonitoring called - Preview: ${frontPreviewView != null}")

        if (!_isInitialized.value) {
            Log.e(TAG, "❌ Cannot start monitoring - camera not initialized")
            return
        }

        if (isMonitoring) {
            Log.w(TAG, "⚠️ Monitoring already active")
            return
        }

        isMonitoring = true
        Log.d(TAG, "✅ isMonitoring = true")

        try {
            // بدء الكاميرا الأمامية مع Face Detection
            startFrontCameraWithDetection(lifecycleOwner, frontPreviewView)

            Log.d(TAG, "✅ Camera monitoring started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start monitoring", e)
            isMonitoring = false
        }
    }

    /**
     * بدء الكاميرا الأمامية مع كشف الوجوه
     */
    @androidx.camera.core.ExperimentalGetImage
    private fun startFrontCameraWithDetection(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView?
    ) {
        cameraManager.startFrontCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            onImageAnalysis = { imageProxy ->
                // تمرير الصورة لـ Face Detection
                // الـ FaceDetectionMonitor سيستدعي callback الـ snapshot تلقائياً
                faceDetectionMonitor.processImage(imageProxy)
            }
        )

        // بدء مراقبة Face Detection
        faceDetectionMonitor.startMonitoring()
        _isFrontCameraActive.value = true

        Log.d(TAG, "Front camera with face detection started")
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        if (!isMonitoring) return

        isMonitoring = false

        faceDetectionMonitor.stopMonitoring()
        cameraManager.stopAllCameras()

        _isFrontCameraActive.value = false
        _isBackCameraActive.value = false

        Log.d(TAG, "Camera monitoring stopped")
    }

    /**
     * إيقاف مؤقت للمراقبة
     */
    fun pauseMonitoring() {
        faceDetectionMonitor.stopMonitoring()
        Log.d(TAG, "Monitoring paused")
    }

    /**
     * استئناف المراقبة
     */
    fun resumeMonitoring() {
        if (isMonitoring && _isFrontCameraActive.value) {
            faceDetectionMonitor.startMonitoring()
            Log.d(TAG, "Monitoring resumed")
        }
    }

    /**
     * الحصول على حالة المراقبة
     */
    fun getMonitoringState() = faceDetectionMonitor.monitoringState

    /**
     * الحصول على آخر نتيجة كشف وجه
     */
    fun getLastDetectionResult() = faceDetectionMonitor.lastDetectionResult

    /**
     * الحصول على إحصائيات المراقبة
     */
    fun getMonitoringStats() = faceDetectionMonitor.getStats()

    /**
     * الحصول على إحصائيات الـ Snapshots
     */
    fun getSnapshotStats() = snapshotManager.snapshotStats

    /**
     * الحصول على عدد الـ snapshots المتبقية
     */
    fun getRemainingSnapshotsCount() = snapshotManager.getRemainingSnapshotsCount()

    /**
     * فحص توفر الكاميرات
     */
    fun checkCameraAvailability(): CameraAvailability {
        return CameraAvailability(
            hasFrontCamera = cameraManager.isCameraAvailable(
                androidx.camera.core.CameraSelector.LENS_FACING_FRONT
            ),
            hasBackCamera = cameraManager.isCameraAvailable(
                androidx.camera.core.CameraSelector.LENS_FACING_BACK
            )
        )
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        stopMonitoring()
        scope.cancel()
        faceDetectionMonitor.cleanup()
        snapshotManager.cleanup()
        cameraManager.cleanup()
        _isInitialized.value = false
        Log.d(TAG, "Camera monitor cleaned up")
    }
}

/**
 * توفر الكاميرات
 */
data class CameraAvailability(
    val hasFrontCamera: Boolean,
    val hasBackCamera: Boolean
) {
    val isFullyAvailable: Boolean
        get() = hasFrontCamera && hasBackCamera
}