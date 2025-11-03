package com.example.saffieduapp.data.firebase

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

/**
 * مسؤول رفع بيانات المراقبة إلى Firebase
 */
class FirebaseUploadRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val TAG = "FirebaseUploadRepo"

    /**
     * رفع جميع الوسائط والتقرير النهائي
     * @param sessionJson تقرير الجلسة بصيغة JSON من ExamSessionManager.exportSessionForUpload()
     * @param mediaFiles قائمة ملفات الوسائط من ExamSessionManager.getLocalMediaFiles()
     * @param examId معرف الاختبار
     * @param studentId معرف الطالب
     * @param sessionId معرف الجلسة
     */
    suspend fun uploadSessionData(
        sessionJson: String,
        mediaFiles: List<File>,
        examId: String,
        studentId: String,
        sessionId: String
    ): Boolean {
        return try {
            // 1️⃣ رفع جميع الوسائط إلى Firebase Storage
            val uploadedMedia = mutableListOf<Map<String, Any>>()

            for (file in mediaFiles) {
                val downloadUrl = uploadMediaFile(file, examId, studentId, sessionId)
                if (downloadUrl != null) {
                    uploadedMedia.add(
                        mapOf(
                            "fileName" to file.name,
                            "fileSize" to file.length(),
                            "downloadUrl" to downloadUrl,
                            "timestamp" to System.currentTimeMillis(),
                            "type" to if (file.extension == "mp4") "video" else "image"
                        )
                    )
                }
            }

            // 2️⃣ رفع التقرير النصي (JSON) إلى Firestore
            val sessionReport = hashMapOf(
                "examId" to examId,
                "studentId" to studentId,
                "sessionId" to sessionId,
                "reportJson" to sessionJson,
                "media" to uploadedMedia,
                "uploadedAt" to System.currentTimeMillis()
            )

            firestore.collection("exam_monitoring_reports")
                .document(sessionId)
                .set(sessionReport)
                .await()

            Log.d(TAG, "✅ Session uploaded successfully to Firestore + Storage")
            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error uploading session data", e)
            false
        }
    }

    /**
     * رفع ملف وسائط واحد إلى Storage
     */
    private suspend fun uploadMediaFile(
        file: File,
        examId: String,
        studentId: String,
        sessionId: String
    ): String? {
        return try {
            val ref = storage.reference.child(
                "exam_monitoring/$examId/$studentId/$sessionId/${file.name}"
            )
            val uri = Uri.fromFile(file)
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Log.d(TAG, "✅ Uploaded ${file.name}: $url")
            url
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to upload media file: ${file.name}", e)
            null
        }
    }
}
