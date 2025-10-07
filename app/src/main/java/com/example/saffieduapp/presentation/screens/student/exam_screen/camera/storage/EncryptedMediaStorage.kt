package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.storage

import android.content.Context
import android.util.Log
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.CameraMonitoringConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 🔐 مدير التخزين المشفر للوسائط
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/storage/EncryptedMediaStorage.kt
 *
 * 🎯 الهدف:
 * حفظ الملفات (فيديو/صور) بشكل مشفر وآمن
 *
 * 🔒 التشفير: AES-256-GCM
 *
 * 📁 الهيكل:
 * /exam_sessions/
 *   /session_123/
 *     /videos/
 *     /snapshots/
 *     /logs/
 *     /metadata/
 */
class EncryptedMediaStorage(
    private val context: Context,
    private val sessionId: String
) {
    private val TAG = "EncryptedStorage"

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_SIZE = 256
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 128
    }

    // ═══════════════════════════════════════════
    // 📁 المجلدات
    // ═══════════════════════════════════════════

    // المجلد الرئيسي
    private val rootDir: File = File(context.filesDir, "exam_sessions")

    // مجلد الجلسة الحالية
    private val sessionDir: File = File(rootDir, sessionId)

    // المجلدات الفرعية
    private val videosDir: File = File(sessionDir, "videos")
    private val snapshotsDir: File = File(sessionDir, "snapshots")
    private val logsDir: File = File(sessionDir, "logs")
    private val metadataDir: File = File(sessionDir, "metadata")

    // 🔑 مفتاح التشفير
    private var encryptionKey: SecretKey? = null

    init {
        initializeDirectories()
        initializeEncryptionKey()
    }

    // ═══════════════════════════════════════════
    // 🏗️ التهيئة
    // ═══════════════════════════════════════════

    /**
     * إنشاء المجلدات
     */
    private fun initializeDirectories() {
        try {
            rootDir.mkdirs()
            sessionDir.mkdirs()
            videosDir.mkdirs()
            snapshotsDir.mkdirs()
            logsDir.mkdirs()
            metadataDir.mkdirs()

            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "✅ Directories created: $sessionDir")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating directories", e)
        }
    }

    /**
     * توليد مفتاح التشفير
     *
     * ⚠️ ملاحظة مهمة:
     * في التطبيق الحقيقي يجب استخدام Android Keystore
     * هنا نستخدم مفتاح مولد للتوضيح فقط
     */
    private fun initializeEncryptionKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance("AES")
            keyGenerator.init(KEY_SIZE)
            encryptionKey = keyGenerator.generateKey()

            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "🔑 Encryption key generated")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating encryption key", e)
        }
    }

    // ═══════════════════════════════════════════
    // 🔒 تشفير الملفات
    // ═══════════════════════════════════════════

    /**
     * تشفير ملف فيديو
     *
     * @param sourceFile الملف الأصلي
     * @return الملف المشفر
     */
    fun encryptVideo(sourceFile: File): Result<File> {
        return try {
            val encryptedFile = File(videosDir, "${sourceFile.nameWithoutExtension}_enc.mp4")
            encryptFile(sourceFile, encryptedFile)

            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "📹 Video encrypted: ${encryptedFile.name}")
                Log.d(TAG, "   Size: ${encryptedFile.length() / 1024} KB")
            }

            Result.success(encryptedFile)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error encrypting video", e)
            Result.failure(e)
        }
    }

    /**
     * تشفير صورة snapshot
     *
     * @param sourceFile الملف الأصلي
     * @param index رقم الصورة (1-10)
     * @return الملف المشفر
     */
    fun encryptSnapshot(sourceFile: File, index: Int): Result<File> {
        return try {
            val timestamp = System.currentTimeMillis()
            val encryptedFile = File(snapshotsDir, "snapshot_${index}_${timestamp}_enc.jpg")
            encryptFile(sourceFile, encryptedFile)

            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "📸 Snapshot encrypted: ${encryptedFile.name}")
                Log.d(TAG, "   Size: ${encryptedFile.length() / 1024} KB")
            }

            Result.success(encryptedFile)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error encrypting snapshot", e)
            Result.failure(e)
        }
    }

    /**
     * التشفير الفعلي باستخدام AES-GCM
     *
     * كيف يعمل؟
     * 1. توليد IV عشوائي (12 بايت)
     * 2. تشفير البيانات بالمفتاح
     * 3. حفظ: [IV + البيانات المشفرة]
     */
    private fun encryptFile(sourceFile: File, destFile: File) {
        val key = encryptionKey ?: throw IllegalStateException("Encryption key not initialized")

        // 1️⃣ توليد IV عشوائي
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)

        // 2️⃣ إعداد cipher للتشفير
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        // 3️⃣ قراءة وتشفير وكتابة
        FileInputStream(sourceFile).use { input ->
            FileOutputStream(destFile).use { output ->
                // كتابة IV أولاً (نحتاجه لفك التشفير لاحقاً)
                output.write(iv)

                // تشفير البيانات قطعة قطعة
                val buffer = ByteArray(8192) // 8 KB buffer
                var bytesRead: Int

                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val encrypted = cipher.update(buffer, 0, bytesRead)
                    if (encrypted != null) {
                        output.write(encrypted)
                    }
                }

                // كتابة الجزء النهائي
                val finalBytes = cipher.doFinal()
                if (finalBytes != null) {
                    output.write(finalBytes)
                }
            }
        }
    }

    // ═══════════════════════════════════════════
    // 🔓 فك التشفير (للاختبار والتحقق)
    // ═══════════════════════════════════════════

    /**
     * فك تشفير ملف
     *
     * @param encryptedFile الملف المشفر
     * @param outputFile ملف الإخراج
     * @return الملف بعد فك التشفير
     */
    fun decryptFile(encryptedFile: File, outputFile: File): Result<File> {
        return try {
            val key = encryptionKey ?: throw IllegalStateException("Encryption key not initialized")

            FileInputStream(encryptedFile).use { input ->
                // 1️⃣ قراءة IV
                val iv = ByteArray(IV_SIZE)
                input.read(iv)

                // 2️⃣ إعداد cipher لفك التشفير
                val cipher = Cipher.getInstance(ALGORITHM)
                val gcmSpec = GCMParameterSpec(TAG_SIZE, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

                // 3️⃣ قراءة وفك التشفير وكتابة
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        val decrypted = cipher.update(buffer, 0, bytesRead)
                        if (decrypted != null) {
                            output.write(decrypted)
                        }
                    }

                    val finalBytes = cipher.doFinal()
                    if (finalBytes != null) {
                        output.write(finalBytes)
                    }
                }
            }

            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error decrypting file", e)
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // 🗑️ إدارة الملفات
    // ═══════════════════════════════════════════

    /**
     * حذف ملف بعد رفعه بنجاح
     */
    fun deleteAfterUpload(file: File): Boolean {
        return try {
            val deleted = file.delete()
            if (deleted && CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "🗑️ File deleted: ${file.name}")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting file", e)
            false
        }
    }

    /**
     * تنظيف جميع ملفات الجلسة
     */
    fun cleanupSession(): Boolean {
        return try {
            val deleted = sessionDir.deleteRecursively()
            if (deleted && CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "🧹 Session cleaned up: $sessionId")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cleaning up session", e)
            false
        }
    }

    /**
     * حساب مساحة التخزين المستخدمة
     */
    fun getSessionStorageSize(): Long {
        return try {
            calculateDirectorySize(sessionDir)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calculating storage size", e)
            0
        }
    }

    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    /**
     * التحقق من توفر مساحة كافية
     */
    fun hasEnoughStorage(requiredBytes: Long): Boolean {
        val availableBytes = sessionDir.usableSpace
        val hasSpace = availableBytes > requiredBytes * 1.2 // 20% هامش أمان

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "💾 Storage check:")
            Log.d(TAG, "   Required: ${requiredBytes / 1024} KB")
            Log.d(TAG, "   Available: ${availableBytes / 1024} KB")
            Log.d(TAG, "   Has enough: $hasSpace")
        }

        return hasSpace
    }

    // ═══════════════════════════════════════════
    // 📝 حفظ Metadata (JSON)
    // ═══════════════════════════════════════════

    /**
     * حفظ بيانات JSON
     */
    fun saveMetadata(filename: String, jsonData: String): Result<File> {
        return try {
            val file = File(metadataDir, "$filename.json")
            file.writeText(jsonData)

            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "📄 Metadata saved: $filename.json")
            }

            Result.success(file)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving metadata", e)
            Result.failure(e)
        }
    }

    /**
     * قراءة بيانات JSON
     */
    fun readMetadata(filename: String): Result<String> {
        return try {
            val file = File(metadataDir, "$filename.json")
            if (file.exists()) {
                Result.success(file.readText())
            } else {
                Result.failure(Exception("Metadata file not found: $filename"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error reading metadata", e)
            Result.failure(e)
        }
    }

    // ═══════════════════════════════════════════
    // 📊 Logging
    // ═══════════════════════════════════════════

    /**
     * إضافة سطر في ملف الـ log
     */
    fun appendLog(message: String) {
        try {
            val logFile = File(logsDir, "security_events.log")
            val timestamp = System.currentTimeMillis()
            val formattedMessage = "[$timestamp] $message\n"
            logFile.appendText(formattedMessage)

            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "📝 Log: $message")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error appending log", e)
        }
    }

    // ═══════════════════════════════════════════
    // ℹ️ معلومات الجلسة
    // ═══════════════════════════════════════════

    /**
     * الحصول على معلومات الجلسة
     */
    fun getSessionInfo(): SessionStorageInfo {
        return SessionStorageInfo(
            sessionId = sessionId,
            sessionPath = sessionDir.absolutePath,
            videoCount = videosDir.listFiles()?.size ?: 0,
            snapshotCount = snapshotsDir.listFiles()?.size ?: 0,
            totalSize = getSessionStorageSize(),
            availableSpace = sessionDir.usableSpace
        )
    }

    /**
     * طباعة معلومات الجلسة
     */
    fun printSessionInfo() {
        val info = getSessionInfo()
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📊 Session Info:")
        Log.d(TAG, "   ID: ${info.sessionId}")
        Log.d(TAG, "   Path: ${info.sessionPath}")
        Log.d(TAG, "   Videos: ${info.videoCount}")
        Log.d(TAG, "   Snapshots: ${info.snapshotCount}")
        Log.d(TAG, "   Total Size: ${info.totalSize / 1024} KB")
        Log.d(TAG, "   Available: ${info.availableSpace / 1024 / 1024} MB")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }
}

/**
 * معلومات تخزين الجلسة
 */
data class SessionStorageInfo(
    val sessionId: String,
    val sessionPath: String,
    val videoCount: Int,
    val snapshotCount: Int,
    val totalSize: Long,
    val availableSpace: Long
)