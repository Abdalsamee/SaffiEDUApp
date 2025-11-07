package com.example.saffieduapp.data.FireBase

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.EncryptionHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ExamUploadWorker
 * 🔹 يقوم برفع بيانات جلسة الاختبار إلى Firebase Firestore + Storage
 * 🔹 يفكّ تشفير الوسائط محليًا قبل رفعها حتى تُعرض بشكل سليم لدى المعلم
 */
class ExamUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "ExamUploadWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val examId = inputData.getString("examId") ?: return@withContext Result.failure()
            val studentId = inputData.getString("studentId") ?: return@withContext Result.failure()
            val sessionId = inputData.getString("sessionId") ?: return@withContext Result.failure()
            val sessionJson = inputData.getString("sessionJson") ?: "{}"
            val mediaPathsJson = inputData.getString("mediaPaths") ?: "[]"

            val mediaPaths = Gson().fromJson(mediaPathsJson, Array<String>::class.java).toList()
            val mediaFiles = mediaPaths.mapNotNull { path ->
                val file = File(path)
                if (file.exists()) file else null
            }

            // 🟢 استخراج مفتاح التشفير من التقرير
            val encryptionKey = try {
                val map = Gson().fromJson<Map<String, Any>>(sessionJson, Map::class.java)
                map["encryptionKey"] as? String
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ No encryptionKey in sessionJson")
                null
            }

            val secretKey = encryptionKey?.let { EncryptionHelper.stringToKey(it) }

            // 🧩 فك التشفير فعليًا
            val decryptedFiles = mutableListOf<File>()
            for (file in mediaFiles) {
                try {
                    val tempFile = File(applicationContext.cacheDir, "decrypted_${file.name}")

                    if (secretKey != null) {
                        val encryptedBytes = file.readBytes()
                        val decryptedBytes = EncryptionHelper.decryptBytes(encryptedBytes, secretKey)

                        // التحقق من صحة الناتج
                        if (decryptedBytes!!.isNotEmpty() && decryptedBytes[0] != 0.toByte()) {
                            tempFile.writeBytes(decryptedBytes)
                            decryptedFiles.add(tempFile)
                            Log.d(TAG, "✅ Decrypted file ready: ${tempFile.name}")
                        } else {
                            Log.e(TAG, "❌ Decrypted data seems empty for ${file.name}")
                        }
                    } else {
                        Log.w(TAG, "⚠️ Missing key — uploaded as-is: ${file.name}")
                        decryptedFiles.add(file)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error decrypting ${file.name}", e)
                }
            }

            // 🧩 رفع الملفات المفكوكة
            val uploadRepo = FirebaseUploadRepository()
            val success = uploadRepo.uploadSessionData(
                sessionJson = sessionJson,
                mediaFiles = decryptedFiles,
                examId = examId,
                studentId = studentId,
                sessionId = sessionId
            )

            // 🧹 تنظيف الملفات المؤقتة
            decryptedFiles.forEach {
                if (it.name.startsWith("decrypted_")) it.delete()
            }

            if (success) {
                Log.d(TAG, "✅ Upload complete successfully")
                Result.success()
            } else {
                Log.e(TAG, "❌ Upload failed")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during upload", e)
            Result.retry()
        }
    }

}
