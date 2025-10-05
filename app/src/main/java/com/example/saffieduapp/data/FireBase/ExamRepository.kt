package com.example.saffieduapp.data.FireBase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class Exam(
    val className: String = "",
    val examTitle: String = "",
    val examType: String = "",
    val examDate: String = "",
    val examStartTime: String = "",
    val examTime: String = "",
    val randomQuestions: Boolean = false,
    val showResultsImmediately: Boolean = false,
    val teacherId: String = "",
    val teacherName: String = "",
    val createdAt: String = ""
)

class ExamRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun addExam(exam: Exam): Boolean {
        return try {
            val email = auth.currentUser?.email ?: return false

            // ابحث عن المعلم في مجموعة teachers بناءً على الإيميل
            val teacherQuery = firestore.collection("teachers")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (teacherQuery.isEmpty) {
                // لم يتم العثور على المعلم
                return false
            }

            val teacherDoc = teacherQuery.documents.first()
            val teacherId = teacherDoc.id
            val teacherName = teacherDoc.getString("fullName") ?: "Unknown"

            // بيانات الامتحان للحفظ في مجموعة exams
            val examData = hashMapOf(
                "className" to exam.className,
                "examTitle" to exam.examTitle,
                "examType" to exam.examType,
                "examDate" to exam.examDate,
                "examStartTime" to exam.examStartTime,
                "examTime" to exam.examTime,
                "randomQuestions" to exam.randomQuestions,
                "showResultsImmediately" to exam.showResultsImmediately,
                "teacherId" to teacherId,
                "teacherName" to teacherName,
                "createdAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).format(Date())
            )

            firestore.collection("exams").add(examData).await()
            true

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}