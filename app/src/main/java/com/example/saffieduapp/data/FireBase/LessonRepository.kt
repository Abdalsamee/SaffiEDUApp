package com.example.saffieduapp.data.FireBase

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LessonRepository @Inject constructor(
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) {


    suspend fun saveLesson(lessonData: Map<String, Any>) {
        firestore.collection("lessons")
            .add(lessonData)
            .await()
    }

    // هذه فقط مثال بسيط، لاحقًا ممكن نستبدلها بـ FCM
    suspend fun sendNotificationToStudents(className: String, title: String, description: String) {
        val notification = mapOf(
            "className" to className,
            "title" to title,
            "message" to description,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("notifications").add(notification).await()
    }

    // --- دالة لإرجاع مرجع في Storage ---
    fun getStorageReference(path: String) = storage.getReference(path)

    // --- دالة لرفع الملف واسترجاع الرابط ---
    suspend fun uploadFile(path: String, fileUri: Uri): String {
        val ref = getStorageReference(path)
        ref.putFile(fileUri).await()
        return ref.downloadUrl.await().toString()
    }
}
