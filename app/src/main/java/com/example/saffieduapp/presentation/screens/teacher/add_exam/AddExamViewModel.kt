package com.example.saffieduapp.presentation.screens.teacher.add_exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.Exam
import com.example.saffieduapp.data.FireBase.ExamRepository
import com.example.saffieduapp.data.local.preferences.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class AddExamViewModel @Inject constructor(
    private val repository: ExamRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(AddExamState())
    val state = _state.asStateFlow()

    init {
        loadExamFromDataStore()
    }

    // تحميل بيانات الاختبار المحفوظة من DataStore عند فتح الصفحة
    private fun loadExamFromDataStore() {
        viewModelScope.launch {
            preferencesManager.getExam().collect { exam ->
                if (exam != null) {
                    _state.update {
                        it.copy(
                            selectedClass = exam.className,
                            examTitle = exam.examTitle,
                            examType = exam.examType,
                            examDate = exam.examDate,
                            examStartTime = exam.examStartTime,
                            examTime = exam.examTime,
                            randomQuestions = exam.randomQuestions,
                            showResultsImmediately = exam.showResultsImmediately,
                            isDraftSaved = true // هذا سيخبر الـ UI أن الزر يجب أن يكون معطلًا
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddExamEvent) {
        when (event) {
            is AddExamEvent.ClassSelected -> _state.update {
                it.copy(selectedClass = event.className, isDraftSaved = false)
            }

            is AddExamEvent.TitleChanged -> _state.update {
                it.copy(examTitle = event.title, isDraftSaved = false)
            }

            is AddExamEvent.TypeChanged -> _state.update {
                it.copy(examType = event.type, isDraftSaved = false)
            }

            is AddExamEvent.DateChanged -> _state.update {
                it.copy(examDate = event.date, isDraftSaved = false)
            }

            is AddExamEvent.StartTimeChanged -> _state.update {
                it.copy(examStartTime = event.time, isDraftSaved = false)
            }

            is AddExamEvent.TimeChanged -> _state.update {
                it.copy(examTime = event.time, isDraftSaved = false)
            }

            is AddExamEvent.RandomQuestionsToggled -> _state.update {
                it.copy(randomQuestions = event.isEnabled, isDraftSaved = false)
            }

            is AddExamEvent.ShowResultsToggled -> _state.update {
                it.copy(showResultsImmediately = event.isEnabled, isDraftSaved = false)
            }
            // ✅ التغيير هنا: لن يتم الحفظ عند الضغط على "التالي"
            is AddExamEvent.NextClicked -> {} // فقط التنقل للشاشة التالية
            is AddExamEvent.SaveDraftClicked -> saveDraft() // يمكن الاحتفاظ بالمسودة
        }
    }


    private fun saveDraft() {
        val current = _state.value
        val exam = Exam(
            className = current.selectedClass,
            examTitle = current.examTitle,
            examType = current.examType,
            examDate = current.examDate,
            examStartTime = current.examStartTime,
            examTime = current.examTime,
            randomQuestions = current.randomQuestions,
            showResultsImmediately = current.showResultsImmediately,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).format(Date())
        )

        viewModelScope.launch {
            preferencesManager.saveExam(exam) // حفظ البيانات في DataStore
            _state.update { it.copy(isDraftSaved = true) } // تعطيل الزر وتغيير نصه
        }
    }

    fun fetchTeacherInfo(onResult: (teacherId: String, teacherName: String) -> Unit) {
        viewModelScope.launch {
            try {
                val email = auth.currentUser?.email ?: ""
                if (email.isBlank()) {
                    onResult("", "")
                    return@launch
                }

                val teacherSnapshot = firestore.collection("teachers")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (teacherSnapshot.isEmpty) {
                    onResult("", "")
                    return@launch
                }

                val teacherDoc = teacherSnapshot.documents.first()
                val teacherId = teacherDoc.id
                val teacherName = teacherDoc.getString("fullName") ?: ""
                onResult(teacherId, teacherName)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult("", "")
            }
        }
    }
}