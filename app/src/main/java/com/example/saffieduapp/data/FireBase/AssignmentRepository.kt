package com.example.saffieduapp.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

data class Assignment(
    val title: String,
    val description: String,
    val dueDate: String,
    val className: String,
    val imageUrl: String? = null
)

class AssignmentRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun saveAssignment(
        title: String,
        description: String,
        dueDate: String,
        className: String,
        imageUri: Uri?,
        imageName: String?
    ): Boolean {
        return try {
            val imageUrl = imageUri?.let { uri ->
                val ref = storage.reference.child("assignments/$imageName")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }

            val assignment = Assignment(
                title = title,
                description = description,
                dueDate = dueDate,
                className = className,
                imageUrl = imageUrl
            )

            firestore.collection("assignments")
                .add(assignment)
                .await()

            true // نجاح الحفظ
        } catch (e: Exception) {
            e.printStackTrace()
            false // فشل الحفظ
        }
    }
}
