package com.example.saffieduapp.presentation.screens.teacher.add_exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.Exam
import com.example.saffieduapp.data.FireBase.ExamRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.tasks.await


@HiltViewModel
class AddExamViewModel @Inject constructor(
    private val repository: ExamRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _state = MutableStateFlow(AddExamState())
    val state = _state.asStateFlow()

    fun onEvent(event: AddExamEvent) {
        when (event) {
            is AddExamEvent.ClassSelected -> _state.update { it.copy(selectedClass = event.className) }
            is AddExamEvent.TitleChanged -> _state.update { it.copy(examTitle = event.title) }
            is AddExamEvent.TypeChanged -> _state.update { it.copy(examType = event.type) }
            is AddExamEvent.DateChanged -> _state.update { it.copy(examDate = event.date) }
            is AddExamEvent.StartTimeChanged -> _state.update { it.copy(examStartTime = event.time) }
            is AddExamEvent.TimeChanged -> _state.update { it.copy(examTime = event.time) }
            is AddExamEvent.RandomQuestionsToggled -> _state.update { it.copy(randomQuestions = event.isEnabled) }
            is AddExamEvent.ShowResultsToggled -> _state.update { it.copy(showResultsImmediately = event.isEnabled) }
            is AddExamEvent.NextClicked -> saveExam()
        }
    }


    private fun saveExam() {
        val current = _state.value
        if (current.examTitle.isBlank() || current.selectedClass.isBlank()) return

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val email = auth.currentUser?.email ?: return@launch

                // 🔹 جلب بيانات المعلم بناءً على البريد الإلكتروني
                val teacherSnapshot = firestore.collection("teachers")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (teacherSnapshot.isEmpty) {
                    _state.update { it.copy(isSaving = false, success = false) }
                    return@launch
                }

                val teacherDoc = teacherSnapshot.documents.first()
                val teacherId = teacherDoc.id
                val teacherName = teacherDoc.getString("fullName") ?: "غير معروف"

                // 🔹 إنشاء كائن الامتحان
                val exam = Exam(
                    className = current.selectedClass,
                    examTitle = current.examTitle,
                    examType = current.examType,
                    examDate = current.examDate,
                    examStartTime = current.examStartTime,
                    examTime = current.examTime,
                    randomQuestions = current.randomQuestions,
                    showResultsImmediately = current.showResultsImmediately,
                    teacherId = teacherId,
                    teacherName = teacherName,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date())
                )

                // 🔹 حفظ الامتحان في المستودع
                repository.addExam(exam)

                _state.update { it.copy(isSaving = false, success = true) }

            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { it.copy(isSaving = false, success = false) }
            }
        }
    }
}