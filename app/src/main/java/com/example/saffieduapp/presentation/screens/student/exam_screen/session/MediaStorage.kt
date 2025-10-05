package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.util.Log
import androidx.camera.core.ImageProxy
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.crypto.SecretKey

/**
 * مدير حفظ الوسائط - حفظ وضغط وتشفير
 */
class MediaStorage(
    private val context: Context,
    private val encryptionKey: SecretKey
) {
    private val TAG = "MediaStorage"

    companion object {
        private const val IMAGE_QUALITY = 80 // جودة ضغط JPEG
        private const val MAX_IMAGE_WIDTH = 1280
        private const val MAX_IMAGE_HEIGHT = 720
    }

    /**
     * حفظ صورة من ImageProxy
     */
    fun saveSnapshot(
        imageProxy: ImageProxy,
        sessionId: String,
        reason: SnapshotReason
    ): MediaSnapshot? {
        return try {
            val snapshotId = UUID.randomUUID().toString()

            // تحويل ImageProxy إلى Bitmap
            val bitmap = imageProxyToBitmap(imageProxy) ?: return null

            // ضغط وحفظ
            val encryptedFile = saveAndEncryptImage(bitmap, sessionId, snapshotId)
                ?: return null

            MediaSnapshot(
                id = snapshotId,
                timestamp = System.currentTimeMillis(),
                encryptedFilePath = encryptedFile.absolutePath,
                reason = reason
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save snapshot", e)
            null
        }
    }

    /**
     * حفظ فيديو من ملف
     */
    fun saveVideo(
        videoFile: File,
        sessionId: String
    ): MediaVideo? {
        return try {
            val videoId = UUID.randomUUID().toString()

            // تشفير الفيديو
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

    /**
     * حفظ وتشفير صورة
     */
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

    /**
     * تشفير ملف فيديو
     */
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

    /**
     * ضغط Bitmap
     */
    private fun compressBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // إذا كانت الصورة صغيرة بالفعل
        if (width <= MAX_IMAGE_WIDTH && height <= MAX_IMAGE_HEIGHT) {
            return bitmap
        }

        // حساب نسبة التصغير
        val ratio = minOf(
            MAX_IMAGE_WIDTH.toFloat() / width,
            MAX_IMAGE_HEIGHT.toFloat() / height
        )

        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * تحويل ImageProxy إلى Bitmap
     */
    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            // تصحيح الدوران
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            if (rotationDegrees != 0) {
                rotateBitmap(bitmap, rotationDegrees.toFloat())
            } else {
                bitmap
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to convert ImageProxy to Bitmap", e)
            null
        }
    }

    /**
     * تدوير Bitmap
     */
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    /**
     * الحصول على مدة الفيديو
     */
    private fun getVideoDuration(videoFile: File): Long {
        // TODO: استخدام MediaMetadataRetriever للحصول على المدة الفعلية
        // حالياً نرجع 0 كـ placeholder
        return 0L
    }

    /**
     * فك تشفير صورة للعرض
     */
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

    /**
     * حذف جميع ملفات الجلسة
     */
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

    /**
     * الحصول على مجلد الوسائط
     */
    private fun getMediaDir(sessionId: String): File {
        val dir = File(context.filesDir, "exam_sessions/$sessionId/media")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * الحصول على مجلد مؤقت
     */
    private fun getTempDir(sessionId: String): File {
        val dir = File(context.cacheDir, "exam_temp/$sessionId")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * الحصول على حجم جميع ملفات الجلسة
     */
    fun getSessionFilesSize(sessionId: String): Long {
        val mediaDir = getMediaDir(sessionId)
        return mediaDir.walkTopDown()
            .filter { it.isFile }
            .map { it.length() }
            .sum()
    }
}