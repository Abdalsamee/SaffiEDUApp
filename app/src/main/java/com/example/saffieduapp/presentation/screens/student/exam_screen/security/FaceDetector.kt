package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.tasks.await

/**
 * كاشف الوجوه باستخدام ML Kit
 */
class FaceDetector {

    private val TAG = "FaceDetector"

    // إعدادات Face Detection
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .setMinFaceSize(0.15f) // الحد الأدنى لحجم الوجه
        .build()

    private val detector = FaceDetection.getClient(options)

    /**
     * تحليل الصورة وكشف الوجوه
     */
    @androidx.camera.core.ExperimentalGetImage
    suspend fun detectFaces(imageProxy: ImageProxy): FaceDetectionResult {
        return try {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                imageProxy.close()
                return FaceDetectionResult.Error("Image is null")
            }

            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            val faces = detector.process(inputImage).await()
            imageProxy.close()

            analyzeFaces(faces)

        } catch (e: Exception) {
            Log.e(TAG, "Face detection failed", e)
            imageProxy.close()
            FaceDetectionResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * تحليل الوجوه المكتشفة
     */
    private fun analyzeFaces(faces: List<Face>): FaceDetectionResult {
        return when {
            faces.isEmpty() -> {
                Log.w(TAG, "No face detected")
                FaceDetectionResult.NoFace
            }

            faces.size > 1 -> {
                Log.w(TAG, "Multiple faces detected: ${faces.size}")
                FaceDetectionResult.MultipleFaces(faces.size)
            }

            else -> {
                val face = faces[0]
                analyzeSingleFace(face)
            }
        }
    }

    /**
     * تحليل وجه واحد
     */
    private fun analyzeSingleFace(face: Face): FaceDetectionResult {
        val headEulerAngleY = face.headEulerAngleY // الدوران حول المحور Y (يمين/يسار)
        val headEulerAngleZ = face.headEulerAngleZ // الدوران حول المحور Z (ميلان)

        // التحقق من اتجاه الوجه
        val isLookingAway = kotlin.math.abs(headEulerAngleY) > 30f

        return if (isLookingAway) {
            Log.w(TAG, "Face looking away - Angle Y: $headEulerAngleY")
            FaceDetectionResult.LookingAway(headEulerAngleY)
        } else {
            Log.d(TAG, "Valid face detected")
            FaceDetectionResult.ValidFace(
                boundingBox = face.boundingBox,
                headEulerAngleY = headEulerAngleY,
                headEulerAngleZ = headEulerAngleZ
            )
        }
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        detector.close()
    }
}

/**
 * نتيجة كشف الوجه
 */
sealed class FaceDetectionResult {
    data class ValidFace(
        val boundingBox: Rect,
        val headEulerAngleY: Float,
        val headEulerAngleZ: Float
    ) : FaceDetectionResult()

    object NoFace : FaceDetectionResult()

    data class MultipleFaces(val count: Int) : FaceDetectionResult()

    data class LookingAway(val angle: Float) : FaceDetectionResult()

    data class Error(val message: String) : FaceDetectionResult()
}