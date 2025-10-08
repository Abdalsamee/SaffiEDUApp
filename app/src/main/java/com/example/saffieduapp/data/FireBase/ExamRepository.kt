package com.example.saffieduapp.data.FireBase

import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import com.google.firebase.auth.FirebaseAuth
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

    suspend fun addExamWithQuestions(exam: Exam): Boolean {
        return try {
            val email = auth.currentUser?.email ?: return false

            val teacherQuery = firestore.collection("teachers")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (teacherQuery.isEmpty) return false

            val teacherDoc = teacherQuery.documents.first()
            val teacherId = teacherDoc.id
            val teacherName = teacherDoc.getString("fullName") ?: "Unknown"

            // تحويل الأسئلة لقائمة Maps
            val questionsData = exam.questions.map { question ->
                hashMapOf(
                    "text" to question.text,
                    "type" to question.type,
                    "points" to question.points,
                    "choices" to question.choices.map { choice ->
                        hashMapOf(
                            "text" to choice.text,
                            "isCorrect" to choice.isCorrect
                        )
                    },
                    "essayAnswer" to question.essayAnswer
                )
            }

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
                "createdAt" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH).format(Date()),
                "questions" to questionsData
            )

            firestore.collection("exams").add(examData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}