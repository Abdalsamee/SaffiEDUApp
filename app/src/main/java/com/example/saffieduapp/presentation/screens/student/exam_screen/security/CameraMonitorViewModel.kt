package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.SessionStats
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.SnapshotStats
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel لإدارة الكاميرا والمراقبة
 * ✅ محدّث: دعم تحميل جلسة موجودة من RoomScanActivity
 */
class CameraMonitorViewModel(
    application: Application,
    private val onViolationDetected: (String) -> Unit,
    examId: String,
    studentId: String,
    existingSessionId: String? = null // ✅ جديد: معرف جلسة موجودة
) : AndroidViewModel(application) {

    private val TAG = "CameraMonitorVM"
    private val context = application.applicationContext

    // ✅ ExamSessionManager
    private val sessionManager = ExamSessionManager(
        context = context,
        examId = examId,
        studentId = studentId
    )

    // CameraMonitor
    private val cameraMonitor = CameraMonitor(
        context = context,
        onViolationDetected = onViolationDetected,
        sessionManager = sessionManager
    )

    init {
        // ✅ إذا كان هناك session موجود، حمّله
        if (existingSessionId != null) {
            viewModelScope.launch {
                val session = sessionManager.loadSession(existingSessionId)
                if (session != null) {
                    Log.d(TAG, """
                        ✅ Loaded existing session:
                        ID: ${session.sessionId}
                        Started: ${session.startTime}
                        Snapshots: ${session.snapshots.size}
                        Has Video: ${session.backCameraVideo != null}
                        Status: ${session.status}
                    """.trimIndent())
                } else {
                    Log.e(TAG, "❌ Failed to load session: $existingSessionId")
                    // إنشاء جلسة جديدة كخطة احتياطية
                    sessionManager.startSession()
                }
            }
        }
    }

    /**
     * تهيئة نظام الكاميرا
     */
    fun initializeCamera() {
        viewModelScope.launch {
            val result = cameraMonitor.initialize()
            if (result.isSuccess) {
                Log.d(TAG, "✅ Camera system initialized")
            } else {
                Log.e(TAG, "❌ Failed to initialize camera: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * بدء جلسة اختبار جديدة
     */
    fun startExamSession() {
        // ✅ فقط إذا لم تكن هناك جلسة موجودة
        if (sessionManager.getCurrentSession() == null) {
            sessionManager.startSession()
            Log.d(TAG, "✅ New exam session started")
        } else {
            Log.d(TAG, "ℹ️ Session already exists, skipping startSession()")
        }
    }

    /**
     * إيقاف مؤقت للجلسة
     */
    fun pauseExamSession() {
        sessionManager.pauseSession()
        cameraMonitor.pauseMonitoring()
        Log.d(TAG, "⏸️ Exam session paused")
    }

    /**
     * استئناف الجلسة
     */
    fun resumeExamSession() {
        sessionManager.resumeSession()
        cameraMonitor.resumeMonitoring()
        Log.d(TAG, "▶️ Exam session resumed")
    }

    /**
     * إنهاء الجلسة
     */
    fun endExamSession() {
        sessionManager.endSession()
        cameraMonitor.stopMonitoring()
        Log.d(TAG, "⏹️ Exam session ended")
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        cameraMonitor.stopMonitoring()
        Log.d(TAG, "🛑 Monitoring stopped")
    }

    /**
     * الحصول على CameraMonitor
     */
    fun getCameraMonitor(): CameraMonitor = cameraMonitor

    /**
     * الحصول على حالة الجلسة
     */
    fun getSessionState() = sessionManager.sessionState

    /**
     * الحصول على إحصائيات الصور
     */
    fun getSnapshotStats(): StateFlow<SnapshotStats>? {
        return cameraMonitor.getSnapshotStats()
    }

    /**
     * الحصول على إحصائيات الجلسة
     */
    fun getSessionStats(): SessionStats? {
        return sessionManager.getSessionStats()
    }

    /**
     * الحصول على حالة المراقبة
     */
    fun getMonitoringState() = cameraMonitor.getMonitoringState()

    /**
     * الحصول على آخر نتيجة كشف وجه
     */
    val lastDetectionResult = cameraMonitor.getLastDetectionResult()

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        cameraMonitor.cleanup()
        sessionManager.cleanup()
        Log.d(TAG, "🧹 Resources cleaned up")
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}