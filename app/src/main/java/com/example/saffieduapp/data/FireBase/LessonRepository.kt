package com.example.saffieduapp.data.FireBase

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LessonRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun saveLesson(data: Map<String, Any?>) {
        firestore.collection("lessons").add(data).await()
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
}
