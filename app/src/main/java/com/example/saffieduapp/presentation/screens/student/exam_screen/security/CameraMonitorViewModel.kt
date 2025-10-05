package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel لإدارة الكاميرا والجلسة
 * ✅ محدّث: يتضمن ExamSessionManager
 */
class CameraMonitorViewModel(
    application: Application,
    private val examId: String,
    private val studentId: String,
    private val onViolationDetected: (String) -> Unit
) : AndroidViewModel(application) {

    // ExamSessionManager
    private val sessionManager = ExamSessionManager(
        context = application,
        examId = examId,
        studentId = studentId
    )

    // CameraMonitor مع SessionManager
    private val cameraMonitor = CameraMonitor(
        context = application,
        onViolationDetected = onViolationDetected,
        sessionManager = sessionManager
    )

    // حالة التهيئة
    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.Idle)
    val initializationState: StateFlow<InitializationState> = _initializationState.asStateFlow()

    // توفر الكاميرات
    private val _cameraAvailability = MutableStateFlow<CameraAvailability?>(null)
    val cameraAvailability: StateFlow<CameraAvailability?> = _cameraAvailability.asStateFlow()

    /**
     * تهيئة الكاميرا والجلسة
     */
    fun initializeCamera() {
        viewModelScope.launch {
            _initializationState.value = InitializationState.Initializing

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
     * بدء جلسة الاختبار
     */
    fun startExamSession() {
        sessionManager.startSession()
    }

    /**
     * إنهاء جلسة الاختبار
     */
    fun endExamSession() {
        sessionManager.endSession()
    }

    /**
     * إيقاف مؤقت للجلسة
     */
    fun pauseExamSession() {
        sessionManager.pauseSession()
        cameraMonitor.pauseMonitoring()
    }

    /**
     * استئناف الجلسة
     */
    fun resumeExamSession() {
        sessionManager.resumeSession()
        cameraMonitor.resumeMonitoring()
    }

    /**
     * إنهاء قسري للجلسة
     */
    fun terminateExamSession(reason: String) {
        sessionManager.terminateSession(reason)
        cameraMonitor.stopMonitoring()
    }

    /**
     * الحصول على CameraMonitor للاستخدام في Activity
     */
    fun getCameraMonitor(): CameraMonitor = cameraMonitor

    /**
     * الحصول على SessionManager
     */
    fun getSessionManager(): ExamSessionManager = sessionManager

    /**
     * الحصول على إحصائيات المراقبة
     */
    fun getMonitoringStats() = cameraMonitor.getMonitoringStats()

    /**
     * الحصول على إحصائيات الـ Snapshots
     */
    fun getSnapshotStats() = cameraMonitor.getSnapshotStats()

    /**
     * الحصول على حالة الجلسة
     */
    fun getSessionState() = sessionManager.sessionState

    /**
     * الحصول على إحصائيات الجلسة
     */
    fun getSessionStats() = sessionManager.getSessionStats()

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
        sessionManager.cleanup()
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