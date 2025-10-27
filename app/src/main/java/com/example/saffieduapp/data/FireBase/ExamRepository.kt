package com.example.saffieduapp.data.FireBase

import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class Choice(
    val text: String = "",
    val isCorrect: Boolean = false
)

data class Question(
    val text: String = "",
    val type: String = "", // مثال: "MULTIPLE_CHOICE" أو "ESSAY"
    val points: Int = 0,
    val choices: List<Choice> = emptyList(),
    val essayAnswer: String? = null
)


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
    val createdAt: String = "",
    val questions: List<QuestionData> = emptyList()
)

class ExamRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun addExamWithQuestions(examDto: ExamDto): Pair<Boolean, String?> {
        return try {
            var teacherId = examDto.teacherId
            var teacherName = examDto.teacherName
            if (teacherId.isBlank()) {
                val email = auth.currentUser?.email ?: ""
                if (email.isNotBlank()) {
                    val tQ = firestore.collection("teachers")
                        .whereEqualTo("email", email)
                        .get()
                        .await()
                    if (!tQ.isEmpty) {
                        val doc = tQ.documents.first()
                        teacherId = doc.id
                        teacherName = doc.getString("fullName") ?: teacherName
                    }
                }
            }
            val questionsData = examDto.questions.map { q ->
                hashMapOf(
                    "id" to (q.id.ifBlank { null }),
                    "text" to q.text,
                    "type" to q.type,
                    "points" to q.points,
                    "choices" to q.choices.map { c ->
                        hashMapOf(
                            "id" to (c.id.ifBlank { null }),
                            "text" to c.text,
                            "isCorrect" to c.isCorrect
                        )
                    },
                    "essayAnswer" to (q.essayAnswer ?: "")
                )
            }
            val examData = hashMapOf(
                "className" to examDto.className,
                "examTitle" to examDto.examTitle,
                "examType" to examDto.examType,
                "examDate" to examDto.examDate,
                "examStartTime" to examDto.examStartTime,
                "examTime" to examDto.examTime,
                "randomQuestions" to examDto.randomQuestions,
                "showResultsImmediately" to examDto.showResultsImmediately,
                "teacherId" to teacherId,
                "teacherName" to teacherName,
                "createdAt" to FieldValue.serverTimestamp(), // server timestamp
                "questions" to questionsData
            )
            firestore.collection("exams").add(examData).await()
            Pair(true, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, e.message)
        }
    }
}