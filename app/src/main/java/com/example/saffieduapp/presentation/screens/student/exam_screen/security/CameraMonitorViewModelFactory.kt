package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory لإنشاء CameraMonitorViewModel مع parameters
 * ✅ محدّث: يتضمن examId و studentId
 */
class CameraMonitorViewModelFactory(
    private val application: Application,
    private val examId: String,
    private val studentId: String,
    private val onViolationDetected: (String) -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraMonitorViewModel::class.java)) {
            return CameraMonitorViewModel(
                application = application,
                examId = examId,
                studentId = studentId,
                onViolationDetected = onViolationDetected
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}