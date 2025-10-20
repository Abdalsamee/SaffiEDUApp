package com.example.saffieduapp.data.FireBase

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class StudentAssignmentSubmission(
    val studentId: String,
    val assignmentId: String,
    val submittedFiles: List<String>,
    val submissionTime: Long = System.currentTimeMillis(),
    val isSubmitted: Boolean = true,
    val notes: String? = null
)

class SubmissionRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    @SuppressLint("Range")
    suspend fun submitAssignment(
        studentId: String,
        assignmentId: String,
        files: List<Uri>,
        context: Context,
        notes: String? = null
    ): Boolean {
        return try {
            // رفع الملفات إلى Storage
            val uploadedUrls = files.map { uri ->
                val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    cursor.moveToFirst()
                    cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                } ?: "file_${System.currentTimeMillis()}"

                val ref = storage.reference.child("assignments/$assignmentId/$studentId/$fileName")
                ref.putFile(uri).await()
                ref.downloadUrl.await().toString()
            }

            // حفظ في Firestore
            val submission = StudentAssignmentSubmission(
                studentId = studentId,
                assignmentId = assignmentId,
                submittedFiles = uploadedUrls,
                notes = notes
            )

            firestore.collection("assignment_submissions")
                .document("$assignmentId-$studentId")
                .set(submission)
                .await()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}