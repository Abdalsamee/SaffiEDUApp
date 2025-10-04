package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory لإنشاء CameraMonitorViewModel مع parameters
 */
class CameraMonitorViewModelFactory(
    private val application: Application,
    private val onViolationDetected: (String) -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraMonitorViewModel::class.java)) {
            return CameraMonitorViewModel(application, onViolationDetected) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}