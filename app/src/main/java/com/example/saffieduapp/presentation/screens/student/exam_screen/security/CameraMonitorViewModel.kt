package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel لإدارة مراقبة الكاميرا
 */
class CameraMonitorViewModel(
    application: Application,
    private val onViolationDetected: (String) -> Unit
) : AndroidViewModel(application) {

    private val cameraMonitor = CameraMonitor(
        context = application.applicationContext,
        onViolationDetected = onViolationDetected
    )

    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.Idle)
    val initializationState: StateFlow<InitializationState> = _initializationState.asStateFlow()

    private val _cameraAvailability = MutableStateFlow<CameraAvailability?>(null)
    val cameraAvailability: StateFlow<CameraAvailability?> = _cameraAvailability.asStateFlow()

    val isInitialized = cameraMonitor.isInitialized
    val isFrontCameraActive = cameraMonitor.isFrontCameraActive
    val isBackCameraActive = cameraMonitor.isBackCameraActive
    val monitoringState = cameraMonitor.getMonitoringState()

    /**
     * تهيئة نظام الكاميرا
     */
    fun initializeCamera() {
        if (_initializationState.value is InitializationState.Initializing) {
            return
        }

        viewModelScope.launch {
            _initializationState.value = InitializationState.Initializing

            // تهيئة الكاميرا أولاً
            val result = cameraMonitor.initialize()

            if (result.isFailure) {
                _initializationState.value = InitializationState.Error(
                    result.exceptionOrNull()?.message ?: "فشل في تهيئة الكاميرا"
                )
                return@launch
            }

            // بعد التهيئة الناجحة، فحص توفر الكاميرات
            val availability = cameraMonitor.checkCameraAvailability()
            _cameraAvailability.value = availability

            if (!availability.hasFrontCamera) {
                _initializationState.value = InitializationState.Error("الكاميرا الأمامية غير متوفرة")
                return@launch
            }

            _initializationState.value = InitializationState.Success
        }
    }

    /**
     * الحصول على CameraMonitor للاستخدام في Activity
     */
    fun getCameraMonitor(): CameraMonitor = cameraMonitor

    /**
     * الحصول على إحصائيات المراقبة
     */
    fun getStats() = cameraMonitor.getMonitoringStats()

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        cameraMonitor.stopMonitoring()
    }

    /**
     * إيقاف مؤقت
     */
    fun pauseMonitoring() {
        cameraMonitor.pauseMonitoring()
    }

    /**
     * استئناف
     */
    fun resumeMonitoring() {
        cameraMonitor.resumeMonitoring()
    }

    override fun onCleared() {
        super.onCleared()
        cameraMonitor.cleanup()
    }
}

/**
 * حالة التهيئة
 */
sealed class InitializationState {
    object Idle : InitializationState()
    object Initializing : InitializationState()
    object Success : InitializationState()
    data class Error(val message: String) : InitializationState()
}