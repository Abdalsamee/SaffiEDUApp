package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory لإنشاء CameraMonitorViewModel
 */
class CameraMonitorViewModelFactory(
    private val application: Application,
    private val onViolationDetected: (String) -> Unit,
    private val examId: String? = null,
    private val studentId: String? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraMonitorViewModel::class.java)) {
            return CameraMonitorViewModel(
                application = application,
                onViolationDetected = onViolationDetected,
                examId = examId.toString(),
                studentId = studentId.toString()
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}