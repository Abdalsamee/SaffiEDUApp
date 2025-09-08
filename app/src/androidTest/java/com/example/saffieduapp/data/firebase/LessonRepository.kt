package com.example.saffieduapp.data.firebase

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LessonRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    suspend fun uploadFile(uri: Uri, path: String): String {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun saveLesson(data: Map<String, Any>) {
        firestore.collection("lessons").add(data).await()
    }
}