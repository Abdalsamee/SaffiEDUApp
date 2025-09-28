package com.example.saffieduapp.presentation.screens.student.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TasksState(error = "فشل في تحميل البيانات"))
    val state = _state.asStateFlow()

    init {
        loadTasks()
    }

    fun onTabSelected(index: Int) {
        _state.update { it.copy(selectedTabIndex = index) }
    }

    fun loadTasks() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // جلب الواجبات الحقيقية من Firestore
                val assignments = assignmentRepository.getAllAssignments()

                // تجميع الواجبات حسب التاريخ
                val assignmentsByDate = groupAssignmentsByDate(assignments)

                // البيانات الاختبارية للامتحانات (يمكن استبدالها لاحقاً)
                val examsByDate = getDummyExams()

                _state.update {
                    it.copy(
                        isLoading = false,
                        assignmentsByDate = assignmentsByDate,
                        examsByDate = examsByDate
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "فشل في تحميل البيانات"
                    )
                }
            }
        }
    }

    private fun groupAssignmentsByDate(assignments: List<AssignmentItem>): Map<String, List<AssignmentItem>> {
        return assignments.groupBy { assignment ->
            // استخدام التاريخ الحالي كنموذج، يمكنك تعديله حسب حاجتك
            formatDateForGrouping(Calendar.getInstance().time)
        }
    }

    private fun formatDateForGrouping(date: Date): String {
        val format = SimpleDateFormat("dd / MM / yyyy، EEEE", Locale("ar"))
        return format.format(date)
    }

    private fun getDummyExams(): Map<String, List<ExamItem>> {
        val timeNow = Calendar.getInstance().timeInMillis
        val timeOneHourAgo = timeNow - (60 * 60 * 1000)

        val dummyExams = listOf(
            ExamItem("e1", "اختبار الوحدة الثانية", "مادة التربية الإسلامية", "", timeNow, ExamStatus.NOT_COMPLETED),
            ExamItem("e2", "اختبار الوحدة الثالثة", "مادة اللغة العربية", "", timeOneHourAgo, ExamStatus.COMPLETED)
        )

        return mapOf(formatDateForGrouping(Calendar.getInstance().time) to dummyExams)
    }
}