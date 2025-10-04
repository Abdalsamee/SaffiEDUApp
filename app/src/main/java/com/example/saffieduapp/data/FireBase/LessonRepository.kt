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
    suspend fun saveLessonAndReturnId(lessonData: Map<String, Any?>): String {
        val docRef = firestore.collection("lessons").document()
        docRef.set(lessonData).await()
        return docRef.id
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
