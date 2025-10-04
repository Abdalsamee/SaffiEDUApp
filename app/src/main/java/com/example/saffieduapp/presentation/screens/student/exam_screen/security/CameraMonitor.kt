package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * مراقب الكاميرا الشامل - يدمج الكاميرا مع Face Detection
 */
class CameraMonitor(
    private val context: Context,
    private val onViolationDetected: (String) -> Unit
) {
    private val TAG = "CameraMonitor"

    private val cameraManager = CameraManager(context)
    private val faceDetectionMonitor = FaceDetectionMonitor(onViolationDetected)

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
        frontPreviewView: PreviewView? = null // ✅ اجعلها nullable
    ) {
        if (!_isInitialized.value) {
            Log.e(TAG, "Cannot start monitoring - camera not initialized")
            return
        }

        if (isMonitoring) {
            Log.w(TAG, "Monitoring already active")
            return
        }

        isMonitoring = true

        try {
            // بدء الكاميرا الأمامية مع Face Detection
            startFrontCameraWithDetection(lifecycleOwner, frontPreviewView)

            // بدء الكاميرا الخلفية
            startBackCameraForSnapshots(lifecycleOwner)

            Log.d(TAG, "Camera monitoring started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start monitoring", e)
            isMonitoring = false
        }
    }

    /**
     * بدء الكاميرا الأمامية مع كشف الوجوه
     */
    @androidx.camera.core.ExperimentalGetImage
    private fun startFrontCameraWithDetection(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView? // ✅ nullable
    ) {
        cameraManager.startFrontCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView, // ✅ تمريرها كما هي
            onImageAnalysis = { imageProxy ->
                // تمرير الصورة لـ Face Detection
                faceDetectionMonitor.processImage(imageProxy)
            }
        )

        // بدء مراقبة Face Detection
        faceDetectionMonitor.startMonitoring()
        _isFrontCameraActive.value = true

        Log.d(TAG, "Front camera with face detection started")
    }

    /**
     * بدء الكاميرا الخلفية للقطات عشوائية
     */
    private fun startBackCameraForSnapshots(lifecycleOwner: LifecycleOwner) {
        cameraManager.startBackCamera(lifecycleOwner)
        _isBackCameraActive.value = true

        // جدولة التقاط صور عشوائية
        scheduleRandomSnapshots()

        Log.d(TAG, "Back camera for snapshots started")
    }

    /**
     * جدولة التقاط صور عشوائية من الكاميرا الخلفية
     */
    private fun scheduleRandomSnapshots() {
        scope.launch {
            while (isMonitoring && _isBackCameraActive.value) {
                // انتظار عشوائي بين 2-5 دقائق
                val randomDelay = (120_000L..300_000L).random()
                delay(randomDelay)

                if (isMonitoring) {
                    captureBackCameraSnapshot()
                }
            }
        }
    }

    /**
     * التقاط صورة من الكاميرا الخلفية
     */
    private suspend fun captureBackCameraSnapshot() {
        try {
            Log.d(TAG, "Capturing back camera snapshot...")
            val image = cameraManager.captureBackCameraImage()

            if (image != null) {
                Log.d(TAG, "Back camera snapshot captured successfully")
                // هنا سيتم حفظ ورفع الصورة لاحقاً
            } else {
                Log.w(TAG, "Failed to capture back camera snapshot")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error capturing back camera snapshot", e)
        }
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