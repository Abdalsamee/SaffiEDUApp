package com.example.saffieduapp.data.FireBase

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.saffieduapp.data.firebase.FirebaseUploadRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Worker مسؤول عن رفع بيانات المراقبة بعد انتهاء الاختبار
 */
class ExamUploadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

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

            val uploadRepo = FirebaseUploadRepository()
            val success = uploadRepo.uploadSessionData(
                sessionJson = sessionJson,
                mediaFiles = mediaFiles,
                examId = examId,
                studentId = studentId,
                sessionId = sessionId
            )

            if (success) {
                Log.d("ExamUploadWorker", "✅ Upload completed successfully")
                return@withContext Result.success()
            } else {
                Log.e("ExamUploadWorker", "❌ Upload failed, will retry")
                return@withContext Result.retry()
            }
        } catch (e: Exception) {
            Log.e("ExamUploadWorker", "❌ Exception during upload", e)
            Result.retry()
        }
    }
}