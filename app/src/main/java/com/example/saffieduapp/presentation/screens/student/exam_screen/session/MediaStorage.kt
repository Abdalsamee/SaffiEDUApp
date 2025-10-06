package com.example.saffieduapp.presentation.screens.student.exam_screen.session

import android.content.Context
import android.graphics.ImageFormat
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.crypto.SecretKey

/**
 * مدير تخزين الوسائط - لحفظ الصور والفيديوهات بشكل آمن
 */
class MediaStorage(
    private val context: Context,
    private val encryptionKey: SecretKey
) {
    private val TAG = "MediaStorage"

    /**
     * حفظ snapshot من ImageData
     */
    fun saveSnapshot(
        imageData: ImageData,
        sessionId: String,
        reason: SnapshotReason
    ): SnapshotRecord? {
        return try {
            // تحويل ImageData إلى JPEG
            val jpegBytes = convertToJpeg(imageData)

            if (jpegBytes == null) {
                Log.e(TAG, "Failed to convert image to JPEG")
                return null
            }

            // إنشاء مجلد للصور
            val snapshotsDir = File(context.filesDir, "exam_snapshots/$sessionId")
            if (!snapshotsDir.exists()) {
                snapshotsDir.mkdirs()
            }

            // اسم الملف
            val snapshotId = UUID.randomUUID().toString()
            val filename = "${reason.name}_${System.currentTimeMillis()}.jpg"
            val snapshotFile = File(snapshotsDir, filename)

            // تشفير الصورة (اختياري)
            val encryptedBytes = EncryptionHelper.encryptBytes(jpegBytes, encryptionKey)
            val bytesToSave = encryptedBytes ?: jpegBytes

            // حفظ الملف
            FileOutputStream(snapshotFile).use { outputStream ->
                outputStream.write(bytesToSave)
            }

            Log.d(TAG, "✅ Snapshot saved: ${snapshotFile.absolutePath}")

            // إنشاء السجل
            SnapshotRecord(
                id = snapshotId,
                timestamp = System.currentTimeMillis(),
                reason = reason,
                filePath = snapshotFile.absolutePath,
                fileSize = snapshotFile.length()
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save snapshot", e)
            null
        }
    }

    /**
     * حفظ فيديو الكاميرا الخلفية
     */
    fun saveBackCameraVideo(videoFile: File, sessionId: String): String? {
        return try {
            if (!videoFile.exists()) {
                Log.e(TAG, "Video file not found: ${videoFile.absolutePath}")
                return null
            }

            // إنشاء مجلد للفيديوهات
            val videoDir = File(context.filesDir, "exam_videos/$sessionId")
            if (!videoDir.exists()) {
                videoDir.mkdirs()
            }

            // نسخ الفيديو
            val destinationFile = File(videoDir, "room_scan_${System.currentTimeMillis()}.mp4")
            videoFile.copyTo(destinationFile, overwrite = true)

            Log.d(TAG, "✅ Video saved: ${destinationFile.absolutePath}")
            destinationFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Failed to save video", e)
            null
        }
    }

    /**
     * تحويل ImageData إلى JPEG
     */
    private fun convertToJpeg(imageData: ImageData): ByteArray? {
        return try {
            val yuvImage = android.graphics.YuvImage(
                combineYuvPlanes(imageData),
                ImageFormat.NV21,
                imageData.width,
                imageData.height,
                null
            )

            val outputStream = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(
                android.graphics.Rect(0, 0, imageData.width, imageData.height),
                85, // جودة 85%
                outputStream
            )

            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Error converting to JPEG", e)
            null
        }
    }

    /**
     * دمج YUV planes إلى NV21 format
     */
    private fun combineYuvPlanes(imageData: ImageData): ByteArray {
        val ySize = imageData.yData.size
        val uvSize = imageData.uData.size + imageData.vData.size

        return ByteArray(ySize + uvSize).apply {
            // نسخ Y plane
            System.arraycopy(imageData.yData, 0, this, 0, ySize)

            // Interleave U and V planes (NV21 format: YYYYYYYY VUVUVU)
            var index = ySize
            for (i in imageData.uData.indices) {
                this[index++] = imageData.vData[i]  // V أولاً
                this[index++] = imageData.uData[i]  // ثم U
            }
        }
    }

    /**
     * قراءة snapshot
     */
    fun loadSnapshot(snapshotRecord: SnapshotRecord): ByteArray? {
        return try {
            val file = File(snapshotRecord.filePath)

            if (!file.exists()) {
                Log.e(TAG, "Snapshot file not found: ${snapshotRecord.filePath}")
                return null
            }

            val encryptedBytes = file.readBytes()

            // فك التشفير
            val decryptedBytes = EncryptionHelper.decryptBytes(encryptedBytes, encryptionKey)

            decryptedBytes ?: encryptedBytes

        } catch (e: Exception) {
            Log.e(TAG, "Failed to load snapshot", e)
            null
        }
    }

    /**
     * حذف ملفات الجلسة
     */
    fun deleteSessionFiles(sessionId: String) {
        try {
            // حذف الصور
            val snapshotsDir = File(context.filesDir, "exam_snapshots/$sessionId")
            if (snapshotsDir.exists()) {
                snapshotsDir.deleteRecursively()
                Log.d(TAG, "✅ Snapshots deleted for session: $sessionId")
            }

            // حذف الفيديوهات
            val videoDir = File(context.filesDir, "exam_videos/$sessionId")
            if (videoDir.exists()) {
                videoDir.deleteRecursively()
                Log.d(TAG, "✅ Videos deleted for session: $sessionId")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete session files", e)
        }
    }

    /**
     * حساب حجم ملفات الجلسة
     */
    fun getSessionStorageSize(sessionId: String): Long {
        var totalSize = 0L

        try {
            // حجم الصور
            val snapshotsDir = File(context.filesDir, "exam_snapshots/$sessionId")
            if (snapshotsDir.exists()) {
                snapshotsDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        totalSize += file.length()
                    }
                }
            }

            // حجم الفيديوهات
            val videoDir = File(context.filesDir, "exam_videos/$sessionId")
            if (videoDir.exists()) {
                videoDir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        totalSize += file.length()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate storage size", e)
        }

        return totalSize
    }
}