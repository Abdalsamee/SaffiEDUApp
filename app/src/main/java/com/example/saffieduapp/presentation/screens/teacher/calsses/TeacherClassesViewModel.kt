package com.example.saffieduapp.presentation.screens.teacher.calsses



import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class TeacherClassesViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(TeacherClassesState())
    val state = _state.asStateFlow()

    init {
        loadClasses()
    }

    private fun loadClasses() {
        // --- بيانات مؤقتة ---
        val dummyClasses = listOf(
            ClassItem(
                classId = "c1",
                className = "الصف الأول",
                subjectName = "تربية اسلامية",
                subjectImageUrl = "",
                quizCount = 4,
                assignmentCount = 5,
                videoLessonCount = 15,
                pdfLessonCount = 15,
                studentCount = 25
            ),
            ClassItem(
                classId = "c2",
                className = "الصف الثاني",
                subjectName = "تربية اسلامية",
                subjectImageUrl = "",
                quizCount = 2,
                assignmentCount = 3,
                videoLessonCount = 10,
                pdfLessonCount = 12,
                studentCount = 22
            ),
            ClassItem(
                classId = "c3",
                className = "الصف الثالث",
                subjectName = "تربية اسلامية",
                subjectImageUrl = "",
                quizCount = 5,
                assignmentCount = 6,
                videoLessonCount = 20,
                pdfLessonCount = 18,
                studentCount = 30

        ))

        _state.value = TeacherClassesState(isLoading = false, classes = dummyClasses)
    }
}