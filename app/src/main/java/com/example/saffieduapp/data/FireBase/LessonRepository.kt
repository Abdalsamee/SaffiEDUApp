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

    // حفظ الدرس وإرجاع مستند ID
    suspend fun saveLessonAndReturnId(lessonData: Map<String, Any>): String {
        val docRef = firestore.collection("lessons")
            .add(lessonData)
            .await()
        return docRef.id
    }
    // هذه فقط مثال بسيط، لاحقًا ممكن نستبدلها بـ FCM
    suspend fun sendNotificationToStudents(subjectId: String, grade: String, title: String, description: String) {
        // 1️⃣ جلب الـ subject لمعرفة الصف
        val subjectSnapshot = firestore.collection("subjects")
            .document(subjectId)
            .get()
            .await()

        val subjectGrade = subjectSnapshot.getString("className") ?: return
        if (subjectGrade != grade) return

        // 2️⃣ جلب الطلاب في الصف
        val studentsSnapshot = firestore.collection("students")
            .whereEqualTo("grade", grade)
            .get()
            .await()

        // 3️⃣ إرسال الإشعار لكل طالب
        for (studentDoc in studentsSnapshot.documents) {
            val studentId = studentDoc.id
            val notification = mapOf(
                "studentId" to studentId,
                "title" to title,
                "message" to description,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("notifications").add(notification).await()
        }
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
