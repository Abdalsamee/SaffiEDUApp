package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * مدير الكاميرا - يدير الكاميرا الأمامية والخلفية
 */
class CameraManager(private val context: Context) {

    private val TAG = "CameraManager"

    private var cameraProvider: ProcessCameraProvider? = null
    private var frontCamera: Camera? = null
    private var backCamera: Camera? = null

    private var frontImageAnalysis: ImageAnalysis? = null
    private var backImageCapture: ImageCapture? = null

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /**
     * تهيئة الكاميرا
     */
    suspend fun initialize() = suspendCancellableCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "Camera provider initialized successfully")
                continuation.resume(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize camera provider", e)
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * بدء الكاميرا الأمامية للمراقبة
     */
    fun startFrontCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        onImageAnalysis: (ImageProxy) -> Unit
    ) {
        val provider = cameraProvider ?: run {
            Log.e(TAG, "Camera provider not initialized")
            return
        }

        try {
            // إيقاف أي استخدام سابق
            provider.unbindAll()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Image Analysis لـ Face Detection
            frontImageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        onImageAnalysis(imageProxy)
                    }
                }

            // تحديد الكاميرا الأمامية
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // ربط الكاميرا
            frontCamera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                frontImageAnalysis
            )

            Log.d(TAG, "Front camera started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start front camera", e)
        }
    }

    /**
     * بدء الكاميرا الخلفية لالتقاط الصور
     */
    fun startBackCamera(
        lifecycleOwner: LifecycleOwner
    ) {
        val provider = cameraProvider ?: run {
            Log.e(TAG, "Camera provider not initialized")
            return
        }

        try {
            // Image Capture
            backImageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // تحديد الكاميرا الخلفية
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // ربط الكاميرا
            backCamera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                backImageCapture
            )

            Log.d(TAG, "Back camera started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start back camera", e)
        }
    }

    /**
     * التقاط صورة من الكاميرا الخلفية
     */
    suspend fun captureBackCameraImage(): ImageProxy? = suspendCancellableCoroutine { continuation ->
        val imageCapture = backImageCapture ?: run {
            Log.e(TAG, "Back camera not initialized")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        // Note: هنا يجب استخدام takePicture مع callback
        // للتبسيط، سنعود لهذا لاحقاً
        continuation.resume(null)
    }

    /**
     * إيقاف جميع الكاميرات
     */
    fun stopAllCameras() {
        try {
            cameraProvider?.unbindAll()
            frontCamera = null
            backCamera = null
            Log.d(TAG, "All cameras stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping cameras", e)
        }
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        stopAllCameras()
        cameraExecutor.shutdown()
    }

    /**
     * فحص توفر الكاميرا
     */
    fun isCameraAvailable(lensFacing: Int): Boolean {
        val provider = cameraProvider ?: return false
        return try {
            provider.hasCamera(
                CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
            )
        } catch (e: Exception) {
            false
        }
    }
}