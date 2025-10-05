package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.crypto.SecretKey

class MediaStorage(
    private val context: Context,
    private val encryptionKey: SecretKey
) {
    private val TAG = "MediaStorage"

    companion object {
        private const val IMAGE_QUALITY = 80
        private const val MAX_IMAGE_WIDTH = 1280
        private const val MAX_IMAGE_HEIGHT = 720
    }

    /**
     * ✅ حفظ snapshot من ImageData
     */
    fun saveSnapshot(
        imageData: ImageData,
        sessionId: String,
        reason: SnapshotReason
    ): MediaSnapshot? {
        return try {
            val snapshotId = UUID.randomUUID().toString()

            // تحويل ImageData إلى Bitmap
            val bitmap = imageDataToBitmap(imageData) ?: run {
                Log.e(TAG, "Failed to convert ImageData to Bitmap")
                return null
            }

            // ضغط وحفظ
            val encryptedFile = saveAndEncryptImage(bitmap, sessionId, snapshotId)
                ?: run {
                    bitmap.recycle()
                    return null
                }

            // تنظيف الذاكرة
            bitmap.recycle()

            MediaSnapshot(
                id = snapshotId,
                timestamp = System.currentTimeMillis(),
                encryptedFilePath = encryptedFile.absolutePath,
                reason = reason
            ).also {
                Log.d(TAG, "✅ Snapshot saved successfully: $snapshotId")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save snapshot", e)
            null
        }
    }

    /**
     * ✅ تحويل ImageData إلى Bitmap
     */
    private fun imageDataToBitmap(imageData: ImageData): Bitmap? {
        return try {
            // التحقق من صيغة الصورة
            if (imageData.format != ImageFormat.YUV_420_888) {
                Log.e(TAG, "Unsupported image format: ${imageData.format}")
                return null
            }

            // حساب الحجم الكامل للبيانات
            val ySize = imageData.yData.size
            val uSize = imageData.uData.size
            val vSize = imageData.vData.size

            val nv21 = ByteArray(ySize + uSize + vSize)

            // نسخ Y
            System.arraycopy(imageData.yData, 0, nv21, 0, ySize)

            // تحويل U و V إلى NV21 format
            if (imageData.vPixelStride == 1) {
                // حالة simple: U و V متتاليين
                System.arraycopy(imageData.vData, 0, nv21, ySize, vSize)
                System.arraycopy(imageData.uData, 0, nv21, ySize + vSize, uSize)
            } else {
                // حالة معقدة: interleaved
                var pos = ySize
                val pixelStride = imageData.vPixelStride
                for (i in 0 until vSize step pixelStride) {
                    if (i < imageData.vData.size) {
                        nv21[pos++] = imageData.vData[i]
                    }
                    if (i < imageData.uData.size) {
                        nv21[pos++] = imageData.uData[i]
                    }
                }
            }

            // تحويل NV21 إلى JPEG ثم Bitmap
            val yuvImage = YuvImage(
                nv21,
                ImageFormat.NV21,
                imageData.width,
                imageData.height,
                null
            )

            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                Rect(0, 0, imageData.width, imageData.height),
                100,
                out
            )

            val imageBytes = out.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // تصحيح الدوران
            if (imageData.rotationDegrees != 0) {
                rotateBitmap(bitmap, imageData.rotationDegrees.toFloat())
            } else {
                bitmap
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert ImageData to Bitmap", e)
            null
        }
    }

    private fun saveAndEncryptImage(
        bitmap: Bitmap,
        sessionId: String,
        snapshotId: String
    ): File? {
        val tempFile = File(getTempDir(sessionId), "temp_$snapshotId.jpg")
        val encryptedFile = File(getMediaDir(sessionId), "$snapshotId.enc")

        return try {
            // ضغط الصورة
            val compressedBitmap = compressBitmap(bitmap)

            // حفظ مؤقت
            FileOutputStream(tempFile).use { output ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, output)
            }

            // تنظيف إذا كانت صورة مضغوطة جديدة
            if (compressedBitmap != bitmap) {
                compressedBitmap.recycle()
            }

            // تشفير
            val success = EncryptionHelper.encryptFile(
                inputFile = tempFile,
                outputFile = encryptedFile,
                key = encryptionKey
            )

            // حذف الملف المؤقت
            tempFile.delete()

            if (success) {
                Log.d(TAG, "✅ Image saved and encrypted: $snapshotId")
                encryptedFile
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save and encrypt image", e)
            tempFile.delete()
            null
        }
    }

    fun saveVideo(
        videoFile: File,
        sessionId: String
    ): MediaVideo? {
        return try {
            val videoId = UUID.randomUUID().toString()
            val encryptedFile = encryptVideoFile(videoFile, sessionId, videoId)
                ?: return null

            val duration = getVideoDuration(videoFile)

            MediaVideo(
                id = videoId,
                timestamp = System.currentTimeMillis(),
                encryptedFilePath = encryptedFile.absolutePath,
                duration = duration
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save video", e)
            null
        }
    }

    private fun encryptVideoFile(
        videoFile: File,
        sessionId: String,
        videoId: String
    ): File? {
        val encryptedFile = File(getMediaDir(sessionId), "$videoId.enc")

        return try {
            val success = EncryptionHelper.encryptFile(
                inputFile = videoFile,
                outputFile = encryptedFile,
                key = encryptionKey
            )

            if (success) {
                Log.d(TAG, "✅ Video encrypted: $videoId")
                encryptedFile
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt video", e)
            null
        }
    }

    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= MAX_IMAGE_WIDTH && height <= MAX_IMAGE_HEIGHT) {
            return bitmap
        }

        val ratio = minOf(
            MAX_IMAGE_WIDTH.toFloat() / width,
            MAX_IMAGE_HEIGHT.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getVideoDuration(videoFile: File): Long {
        // TODO: استخدام MediaMetadataRetriever
        return 0L
    }

    fun decryptImage(encryptedFile: File): Bitmap? {
        val tempFile = File(context.cacheDir, "temp_decrypt_${UUID.randomUUID()}.jpg")

        return try {
            val success = EncryptionHelper.decryptFile(
                encryptedFile = encryptedFile,
                outputFile = tempFile,
                key = encryptionKey
            )

            if (success) {
                val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                tempFile.delete()
                bitmap
            } else {
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to decrypt image", e)
            tempFile.delete()
            null
        }
    }

    fun deleteSessionFiles(sessionId: String) {
        try {
            val mediaDir = getMediaDir(sessionId)
            if (mediaDir.exists()) {
                mediaDir.deleteRecursively()
                Log.d(TAG, "✅ Session files deleted: $sessionId")
            }

            val tempDir = getTempDir(sessionId)
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete session files", e)
        }
    }

    private fun getMediaDir(sessionId: String): File {
        val dir = File(context.filesDir, "exam_sessions/$sessionId/media")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun getTempDir(sessionId: String): File {
        val dir = File(context.cacheDir, "exam_temp/$sessionId")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getSessionFilesSize(sessionId: String): Long {
        val mediaDir = getMediaDir(sessionId)
        return mediaDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
}