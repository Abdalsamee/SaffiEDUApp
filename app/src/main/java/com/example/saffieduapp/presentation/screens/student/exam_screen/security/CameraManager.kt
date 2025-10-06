package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
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
        Log.d(TAG, "Starting camera initialization...")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                Log.d(TAG, "✅ Camera provider initialized successfully")

                // فحص الكاميرات المتاحة
                val hasFront = hasCamera(CameraSelector.LENS_FACING_FRONT)
                val hasBack = hasCamera(CameraSelector.LENS_FACING_BACK)

                Log.d(TAG, "Camera availability: Front=$hasFront, Back=$hasBack")

                continuation.resume(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to initialize camera provider", e)
                continuation.resumeWithException(e)
            }
        }, ContextCompat.getMainExecutor(context))

        continuation.invokeOnCancellation {
            Log.w(TAG, "Camera initialization cancelled")
        }
    }

    /**
     * فحص وجود كاميرا معينة
     */
    private fun hasCamera(lensFacing: Int): Boolean {
        val provider = cameraProvider ?: return false
        return try {
            provider.hasCamera(
                CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking camera for lens facing $lensFacing", e)
            false
        }
    }

    /**
     * بدء الكاميرا الأمامية للمراقبة
     */
    fun startFrontCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView?, // ✅ nullable
        onImageAnalysis: (ImageProxy) -> Unit
    ) {
        val provider = cameraProvider ?: run {
            Log.e(TAG, "❌ Camera provider not initialized")
            return
        }

        try {
            Log.d(TAG, "Starting front camera...")

            // إيقاف أي استخدام سابق
            provider.unbindAll()

            // Image Analysis لـ Face Detection (هذا الأهم)
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

            // إذا كان هناك preview، أضفه
            if (previewView != null) {
                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // ربط الكاميرا مع Preview
                frontCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    frontImageAnalysis
                )
            } else {
                // ربط الكاميرا بدون Preview (مراقبة فقط)
                frontCamera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    frontImageAnalysis
                )
            }

            Log.d(TAG, "✅ Front camera started successfully (Preview: ${previewView != null})")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start front camera", e)
        }
    }

    /**
     * بدء الكاميرا الخلفية لالتقاط الصور
     */
    fun startBackCamera(
        lifecycleOwner: LifecycleOwner
    ) {
        val provider = cameraProvider ?: run {
            Log.e(TAG, "❌ Camera provider not initialized")
            return
        }

        try {
            Log.d(TAG, "Starting back camera...")

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

            Log.d(TAG, "✅ Back camera started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start back camera", e)
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
            Log.d(TAG, "✅ All cameras stopped")
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
        Log.d(TAG, "✅ Camera manager cleaned up")
    }

    /**
     * فحص توفر الكاميرا
     */
    fun isCameraAvailable(lensFacing: Int): Boolean {
        val isAvailable = hasCamera(lensFacing)
        Log.d(TAG, "Camera availability check for lens $lensFacing: $isAvailable")
        return isAvailable
    }
}