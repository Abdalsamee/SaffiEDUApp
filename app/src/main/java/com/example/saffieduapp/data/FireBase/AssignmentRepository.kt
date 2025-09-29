package com.example.saffieduapp.data.repository

import android.net.Uri
import com.example.saffieduapp.presentation.screens.student.tasks.AssignmentItem
import com.example.saffieduapp.presentation.screens.student.tasks.AssignmentStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class Assignment(
    val title: String,
    val description: String,
    val dueDate: String,
    val className: String,
    val imageUrl: String? = null
)

class AssignmentRepository @Inject constructor() {

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

    suspend fun getAllAssignments(classNameFilter: String? = null): List<AssignmentItem> {
        return try {
            val snapshot = firestore.collection("assignments").get().await()
            snapshot.documents.mapNotNull { document ->
                val className = document.getString("className") ?: document.getString("ClassName") ?: ""

                // إذا تم تمرير الفلتر ولم يتطابق الصف، تجاهل هذا الواجب
                if (classNameFilter != null && className != classNameFilter) return@mapNotNull null

                val description = document.getString("description") ?: ""
                val dueDate = document.getString("dueDate") ?: document.getString("duebate") ?: ""
                val imageUrl = document.getString("imageUrl") ?: ""
                val title = document.getString("title") ?: ""

                AssignmentItem(
                    id = document.id,
                    title = title,
                    subjectName = className,
                    imageUrl = imageUrl,
                    dueDate = formatDueDate(dueDate),
                    remainingTime = calculateRemainingTime(dueDate),
                    status = calculateAssignmentStatus(dueDate)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    suspend fun getStudentClass(studentId: String): String? {
        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("students")
                .document(studentId)
                .get()
                .await()

            doc.getString("grade") // اسم الحقل الذي يحتوي الصف
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun formatDueDate(dueDate: String): String {
        return try {
            // تحويل التاريخ من "2025-09-22" إلى "ينتهي في: 22 سبتمبر 2025"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
            val date = inputFormat.parse(dueDate)
            "ينتهي في: ${outputFormat.format(date)}"
        } catch (e: Exception) {
            "ينتهي في: $dueDate"
        }
    }

    private fun calculateRemainingTime(dueDate: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val due = dateFormat.parse(dueDate)
            val today = Calendar.getInstance().time

            val diff = due.time - today.time
            val days = (diff / (24 * 60 * 60 * 1000)).toInt()

            when {
                days < 0 -> "منتهي"
                days == 0 -> "ينتهي اليوم"
                days == 1 -> "متبقي يوم واحد"
                days <= 7 -> "متبقي $days أيام"
                else -> "متبقي ${days/7} أسابيع"
            }
        } catch (e: Exception) {
            "غير محدد"
        }
    }

    private fun calculateAssignmentStatus(dueDate: String): AssignmentStatus {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val due = dateFormat.parse(dueDate)
            val today = Calendar.getInstance().time

            val diff = due.time - today.time
            val days = (diff / (24 * 60 * 60 * 1000)).toInt()

            when {
                days < 0 -> AssignmentStatus.EXPIRED
                days == 0 -> AssignmentStatus.LATE
                else -> AssignmentStatus.PENDING
            }
        } catch (e: Exception) {
            AssignmentStatus.PENDING
        }
    }
}