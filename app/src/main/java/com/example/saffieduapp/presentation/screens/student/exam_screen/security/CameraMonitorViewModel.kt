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
 * ViewModel لإدارة مراقبة الكاميرا
 * ✅ متوافق مع الكود الأصلي + دعم اختياري للجلسات
 */
class CameraMonitorViewModel(
    application: Application,
    private val onViolationDetected: (String) -> Unit,
    // ✅ Parameters اختيارية للجلسات
    private val examId: String? = null,
    private val studentId: String? = null
) : AndroidViewModel(application) {

    // ✅ ExamSessionManager اختياري
    private val sessionManager: ExamSessionManager? = if (!examId.isNullOrEmpty() && !studentId.isNullOrEmpty()) {
        ExamSessionManager(
            context = application.applicationContext,
            examId = examId,
            studentId = studentId
        )
    } else {
        null
    }

    // CameraMonitor مع SessionManager اختياري
    private val cameraMonitor = CameraMonitor(
        context = application.applicationContext,
        onViolationDetected = onViolationDetected,
        sessionManager = sessionManager!!
    )

    private val _initializationState = MutableStateFlow<InitializationState>(InitializationState.Idle)
    val initializationState: StateFlow<InitializationState> = _initializationState.asStateFlow()

    private val _cameraAvailability = MutableStateFlow<CameraAvailability?>(null)
    val cameraAvailability: StateFlow<CameraAvailability?> = _cameraAvailability.asStateFlow()

    // ✅ الخصائص الأساسية من GitHub
    val isInitialized = cameraMonitor.isInitialized
    val isFrontCameraActive = cameraMonitor.isFrontCameraActive
    val isBackCameraActive = cameraMonitor.isBackCameraActive
    val monitoringState = cameraMonitor.getMonitoringState()
    val lastDetectionResult = cameraMonitor.getLastDetectionResult() // ✅ هذا المطلوب

    /**
     * تهيئة نظام الكاميرا
     */
    fun initializeCamera() {
        if (_initializationState.value is InitializationState.Initializing) {
            return
        }

        viewModelScope.launch {
            _initializationState.value = InitializationState.Initializing

            val result = cameraMonitor.initialize()

            if (result.isFailure) {
                _initializationState.value = InitializationState.Error(
                    result.exceptionOrNull()?.message ?: "فشل في تهيئة الكاميرا"
                )
                return@launch
            }

            val availability = cameraMonitor.checkCameraAvailability()
            _cameraAvailability.value = availability

            if (!availability.hasFrontCamera) {
                _initializationState.value = InitializationState.Error("الكاميرا الأمامية غير متوفرة")
                return@launch
            }

            _initializationState.value = InitializationState.Success
        }
    }

    // ✅ دوال الجلسة (تعمل فقط إذا كان SessionManager موجوداً)
    fun startExamSession() = sessionManager?.startSession()
    fun endExamSession() = sessionManager?.endSession()
    fun pauseExamSession() {
        sessionManager?.pauseSession()
        cameraMonitor.pauseMonitoring()
    }
    fun resumeExamSession() {
        sessionManager?.resumeSession()
        cameraMonitor.resumeMonitoring()
    }
    fun terminateExamSession(reason: String) {
        sessionManager?.terminateSession(reason)
        cameraMonitor.stopMonitoring()
    }

    /**
     * الحصول على CameraMonitor
     */
    fun getCameraMonitor(): CameraMonitor = cameraMonitor

    /**
     * الحصول على SessionManager (nullable)
     */
    fun getSessionManager(): ExamSessionManager? = sessionManager

    /**
     * الحصول على حالة الجلسة (nullable)
     */
    fun getSessionState() = sessionManager?.sessionState

    /**
     * الحصول على إحصائيات الجلسة (nullable)
     */
    fun getSessionStats() = sessionManager?.getSessionStats()

    /**
     * الحصول على إحصائيات الـ Snapshots (nullable)
     */
    fun getSnapshotStats() = cameraMonitor.getSnapshotStats()

    /**
     * الحصول على إحصائيات المراقبة
     */
    fun getStats() = cameraMonitor.getMonitoringStats()

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() = cameraMonitor.stopMonitoring()

    /**
     * إيقاف مؤقت
     */
    fun pauseMonitoring() = cameraMonitor.pauseMonitoring()

    /**
     * استئناف
     */
    fun resumeMonitoring() = cameraMonitor.resumeMonitoring()

    override fun onCleared() {
        super.onCleared()
        cameraMonitor.cleanup()
        sessionManager?.cleanup()
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