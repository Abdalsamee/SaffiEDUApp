package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * مصنع ViewModel للكاميرا
 * ✅ محدّث: دعم تحميل جلسة موجودة
 */
class CameraMonitorViewModelFactory(
    private val application: Application,
    private val onViolationDetected: (String) -> Unit,
    private val examId: String,
    private val studentId: String,
    private val existingSessionId: String? = null // ✅ جديد: معرف جلسة موجودة
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraMonitorViewModel::class.java)) {
            return CameraMonitorViewModel(
                application = application,
                onViolationDetected = onViolationDetected,
                examId = examId,
                studentId = studentId,
                existingSessionId = existingSessionId // ✅ تمرير الجلسة الموجودة
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}