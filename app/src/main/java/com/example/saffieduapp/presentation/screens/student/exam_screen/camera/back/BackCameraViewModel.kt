package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.back

import android.app.Application
import android.util.Log
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.CameraMonitoringConfig
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.models.BackCameraRecording
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.storage.EncryptedMediaStorage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 🧠 ViewModel للكاميرا الخلفية
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/back/BackCameraViewModel.kt
 *
 * 🎯 الهدف:
 * ربط المجدول + المسجل + الواجهة معاً
 *
 * 📊 المسؤوليات:
 * 1. إدارة دورة حياة المجدول
 * 2. إدارة دورة حياة المسجل
 * 3. تنسيق العمليات
 * 4. إرسال الأحداث للـ UI
 */
class BackCameraViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "BackCameraVM"

    // المكونات
    private var scheduler: BackCameraScheduler? = null
    private var recorder: BackCameraRecorder? = null
    private var storage: EncryptedMediaStorage? = null

    // حالة النظام
    private val _systemState = MutableStateFlow<BackCameraSystemState>(BackCameraSystemState.Idle)
    val systemState: StateFlow<BackCameraSystemState> = _systemState.asStateFlow()

    // حالة التسجيل
    private val _recordingState = MutableStateFlow<BackCameraRecorder.RecordingState>(
        BackCameraRecorder.RecordingState.Idle
    )
    val recordingState: StateFlow<BackCameraRecorder.RecordingState> = _recordingState.asStateFlow()

    // الأحداث
    private val _events = MutableSharedFlow<BackCameraEvent>()
    val events: SharedFlow<BackCameraEvent> = _events.asSharedFlow()

    // معلومات الجلسة
    private var sessionId: String = ""
    private var examId: String = ""
    private var studentId: String = ""

    // ═══════════════════════════════════════════
    // 🏗️ التهيئة
    // ═══════════════════════════════════════════

    /**
     * تهيئة النظام
     */
    fun initialize(
        sessionId: String,
        examId: String,
        studentId: String,
        examDurationMs: Long
    ) {
        this.sessionId = sessionId
        this.examId = examId
        this.studentId = studentId

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🚀 Initializing Back Camera System")
            Log.d(TAG, "   Session: $sessionId")
            Log.d(TAG, "   Exam: $examId")
            Log.d(TAG, "   Duration: ${examDurationMs / 60000}min")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        }

        // إنشاء التخزين المشفر
        storage = EncryptedMediaStorage(
            context = getApplication(),
            sessionId = sessionId
        )

        // إنشاء المجدول
        scheduler = BackCameraScheduler(
            examDurationMs = examDurationMs,
            onRecordingTime = {
                // عند حلول وقت التسجيل
                handleRecordingTimeReached()
            }
        )

        // بدء المجدول
        scheduler?.start()

        _systemState.value = BackCameraSystemState.Scheduled

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            val info = scheduler?.getScheduleInfo()
            Log.d(TAG, "⏰ Scheduler started")
            Log.d(TAG, "   Scheduled at: ${info?.scheduledTimeFromStart}")
        }
    }

    // ═══════════════════════════════════════════
    // 🎬 معالجة وقت التسجيل
    // ═══════════════════════════════════════════

    /**
     * يتم استدعاؤها عندما يحين وقت التسجيل
     */
    private fun handleRecordingTimeReached() {
        viewModelScope.launch {
            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "🎬 Recording time reached!")
            }

            // إرسال حدث لإيقاف الاختبار مؤقتاً
            _events.emit(BackCameraEvent.PauseExam)

            // تغيير الحالة
            _systemState.value = BackCameraSystemState.ReadyToRecord
        }
    }

    // ═══════════════════════════════════════════
    // 📹 عمليات التسجيل
    // ═══════════════════════════════════════════

    /**
     * تهيئة الكاميرا للتسجيل
     */
    fun prepareCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: androidx.camera.view.PreviewView
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "📹 Preparing camera for recording...")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // إنشاء المسجل
                recorder = BackCameraRecorder(
                    context = getApplication(),
                    storage = storage!!,
                    sessionId = sessionId,
                    examId = examId,
                    studentId = studentId
                )

                Log.d(TAG, "✅ Recorder created")

                // تهيئة الكاميرا مع PreviewView
                val result = recorder!!.initialize(lifecycleOwner, previewView)

                if (result.isSuccess) {
                    _systemState.value = BackCameraSystemState.CameraReady
                    Log.d(TAG, "✅ Camera initialized successfully")

                    // مراقبة حالة التسجيل
                    observeRecordingState()

                    // إرسال حدث لعرض شاشة المسح
                    _events.emit(BackCameraEvent.ShowScanScreen)
                    Log.d(TAG, "📺 Show scan screen event emitted")

                } else {
                    throw Exception("Failed to initialize camera")
                }

            } catch (e: Exception) {
                Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.e(TAG, "❌ Error preparing camera", e)
                Log.e(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                _systemState.value = BackCameraSystemState.Error(e.message ?: "خطأ غير معروف")
                _events.emit(BackCameraEvent.Error(e.message ?: "خطأ في تهيئة الكاميرا"))
            }
        }
    }

    /**
     * مراقبة حالة التسجيل
     */
    private fun observeRecordingState() {
        viewModelScope.launch {
            recorder?.state?.collect { state ->
                _recordingState.value = state

                // معالجة الحالات
                when (state) {
                    is BackCameraRecorder.RecordingState.Recording -> {
                        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                            Log.d(TAG, "⏺️ Recording: ${state.elapsedSeconds}s")
                        }
                    }

                    is BackCameraRecorder.RecordingState.Completed -> {
                        handleRecordingCompleted(state.recording)
                    }

                    is BackCameraRecorder.RecordingState.Error -> {
                        Log.e(TAG, "❌ Recording error: ${state.message}")
                        _events.emit(BackCameraEvent.Error(state.message))
                    }

                    else -> { /* حالات أخرى */ }
                }
            }
        }
    }

    /**
     * بدء التسجيل
     */
    fun startRecording() {
        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "🎬 Starting recording...")
        }

        _systemState.value = BackCameraSystemState.Recording
        recorder?.startRecording()
    }

    /**
     * معالجة اكتمال التسجيل
     */
    private fun handleRecordingCompleted(recording: BackCameraRecording) {
        viewModelScope.launch {
            if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                Log.d(TAG, "✅ Recording completed!")
                Log.d(TAG, "   ID: ${recording.id}")
                Log.d(TAG, "   Size: ${recording.fileSize / 1024} KB")
                Log.d(TAG, "   Encrypted: ${recording.encryptedFilePath}")
            }

            _systemState.value = BackCameraSystemState.Completed(recording)

            // إرسال حدث للعودة للاختبار
            _events.emit(BackCameraEvent.ResumeExam)
        }
    }

    // ═══════════════════════════════════════════
    // 🧹 التنظيف
    // ═══════════════════════════════════════════

    /**
     * تنظيف الموارد
     */
    override fun onCleared() {
        super.onCleared()

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "🧹 Cleaning up...")
        }

        scheduler?.cleanup()
        recorder?.cleanup()

        scheduler = null
        recorder = null
        storage = null
    }

    /**
     * الحصول على PreviewView (للـ UI)
     */
    fun getPreviewView(): PreviewView {
        return PreviewView(getApplication())
    }
}

// ═══════════════════════════════════════════
// 📊 حالات النظام
// ═══════════════════════════════════════════

sealed class BackCameraSystemState {
    data object Idle : BackCameraSystemState()
    data object Scheduled : BackCameraSystemState()
    data object ReadyToRecord : BackCameraSystemState()
    data object CameraReady : BackCameraSystemState()
    data object Recording : BackCameraSystemState()
    data class Completed(val recording: BackCameraRecording) : BackCameraSystemState()
    data class Error(val message: String) : BackCameraSystemState()
}

// ═══════════════════════════════════════════
// 📡 الأحداث
// ═══════════════════════════════════════════

sealed class BackCameraEvent {
    data object PauseExam : BackCameraEvent()
    data object ShowScanScreen : BackCameraEvent()
    data object ResumeExam : BackCameraEvent()
    data class Error(val message: String) : BackCameraEvent()
}