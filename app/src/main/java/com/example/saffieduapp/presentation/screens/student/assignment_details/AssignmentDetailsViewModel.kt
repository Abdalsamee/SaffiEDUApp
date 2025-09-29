package com.example.saffieduapp.presentation.screens.student.assignment_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AssignmentDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AssignmentDetailsState())
    val state = _state.asStateFlow()

    private val db = FirebaseFirestore.getInstance()

    init {
        val assignmentId = savedStateHandle.get<String>("assignmentId")
        if (assignmentId != null) {
            loadAssignmentDetails(assignmentId)
        }
    }

    private fun loadAssignmentDetails(id: String) {
        _state.value = AssignmentDetailsState(isLoading = true)

        // استخدام Firestore لجلب بيانات الواجب
        db.collection("assignments")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val title = document.getString("title") ?: ""
                    val description = document.getString("description")
                    val imageUrl = document.getString("imageUrl")
                    val subjectName = document.getString("className") ?: ""
                    val dueDate = document.getString("dueDate") ?: ""
                    val teacherName = document.getString("teacherName") ?: "غير محدد" // إذا خزنت اسم المعلم

                    // حساب الوقت المتبقي
                    val remainingTime = calculateRemainingTime(dueDate)

                    val details = AssignmentDetails(
                        id = id,
                        title = title,
                        description = description,
                        imageUrl = imageUrl,
                        subjectName = subjectName,
                        teacherName = teacherName,
                        dueDate = formatDueDate(dueDate),
                        remainingTime = remainingTime
                    )

                    _state.value = AssignmentDetailsState(isLoading = false, assignmentDetails = details)
                } else {
                    _state.value = AssignmentDetailsState(isLoading = false, assignmentDetails = null)
                }
            }
            .addOnFailureListener {
                _state.value = AssignmentDetailsState(isLoading = false, assignmentDetails = null)
            }
    }

    private fun formatDueDate(dueDate: String): String {
        return try {
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
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val due = dateFormat.parse(dueDate)

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            val diff = due.time - today.time
            val days = (diff / (24 * 60 * 60 * 1000)).toInt()

            when {
                days < 0 -> "منتهي"
                days == 0 -> "ينتهي اليوم"
                days == 1 -> "متبقي يوم واحد"
                days <= 7 -> "متبقي $days أيام"
                else -> "متبقي ${days / 7} أسابيع"
            }
        } catch (e: Exception) {
            "غير محدد"
        }
    }
}