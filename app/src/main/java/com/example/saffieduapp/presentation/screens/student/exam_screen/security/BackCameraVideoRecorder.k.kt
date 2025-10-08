package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

/**
 * مسجل فيديو الكاميرا الخلفية - لمسح الغرفة
 */
class BackCameraVideoRecorder(
    private val context: Context,
    private val sessionManager: ExamSessionManager
) {
    private val TAG = "BackCameraRecorder"

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val executor = Executors.newSingleThreadExecutor()

    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    companion object {
        private const val ROOM_SCAN_DURATION = 10_000L // 30 ثانية
    }

    /**
     * بدء مسح الغرفة
     */
    suspend fun startRoomScan(
        lifecycleOwner: LifecycleOwner,
        sessionId: String
    ): Result<File> {
        return try {
            Log.d(TAG, "Starting room scan...")

            cameraProvider = ProcessCameraProvider.getInstance(context).get()

            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HD,
                        FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                    )
                )
                .setExecutor(executor)
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val backCamera = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                backCamera,
                videoCapture
            )

            val videoFile = createVideoFile(sessionId)
            startRecording(videoFile)
            scheduleAutoStop()

            Log.d(TAG, "✅ Room scan started")
            Result.success(videoFile)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start room scan", e)
            _recordingState.value = RecordingState.ERROR(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    /**
     * بدء التسجيل
     */
    private fun startRecording(videoFile: File) {
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        activeRecording = videoCapture?.output
            ?.prepareRecording(context, outputOptions)
            ?.start(ContextCompat.getMainExecutor(context)) { event ->
                handleRecordingEvent(event, videoFile)
            }

        _recordingState.value = RecordingState.RECORDING
        startDurationCounter()

        Log.d(TAG, "Recording started: ${videoFile.name}")
    }

    /**
     * معالجة أحداث التسجيل
     */
    private fun handleRecordingEvent(event: VideoRecordEvent, videoFile: File) {
        when (event) {
            is VideoRecordEvent.Start -> {
                Log.d(TAG, "Recording event: START")
            }

            is VideoRecordEvent.Status -> {
                _recordingDuration.value = event.recordingStats.recordedDurationNanos / 1_000_000
            }

            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    Log.e(TAG, "Recording error: ${event.error}")
                    _recordingState.value = RecordingState.ERROR(
                        "Recording failed: ${event.error}"
                    )
                } else {
                    Log.d(TAG, "✅ Recording completed: ${videoFile.absolutePath}")
                    _recordingState.value = RecordingState.COMPLETED(videoFile)

                    scope.launch(Dispatchers.IO) {
                        val saved = sessionManager.saveBackCameraVideo(videoFile)
                        if (saved) {
                            Log.d(TAG, "✅ Video saved to session")
                        } else {
                            Log.e(TAG, "❌ Failed to save video to session")
                        }
                    }
                }
            }
        }
    }

    /**
     * إيقاف التسجيل
     */
    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
        _recordingState.value = RecordingState.STOPPED
        Log.d(TAG, "Recording stopped")
    }

    /**
     * جدولة الإيقاف التلقائي
     */
    private fun scheduleAutoStop() {
        scope.launch {
            kotlinx.coroutines.delay(ROOM_SCAN_DURATION)
            stopRecording()
        }
    }

    /**
     * عداد المدة
     */
    private fun startDurationCounter() {
        scope.launch {
            while (_recordingState.value == RecordingState.RECORDING) {
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    /**
     * إنشاء ملف الفيديو
     */
    private fun createVideoFile(sessionId: String): File {
        val videoDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "exam_videos"
        )

        if (!videoDir.exists()) {
            videoDir.mkdirs()
        }

        return File(
            videoDir,
            "room_scan_${sessionId}_${System.currentTimeMillis()}.mp4"
        )
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        stopRecording()
        cameraProvider?.unbindAll()
        cameraProvider = null
        executor.shutdown()
        Log.d(TAG, "Resources cleaned up")
    }
}

/**
 * حالات التسجيل
 */
sealed class RecordingState {
    object IDLE : RecordingState()
    object RECORDING : RecordingState()
    object STOPPED : RecordingState()
    data class COMPLETED(val videoFile: File) : RecordingState()
    data class ERROR(val message: String) : RecordingState()
}

/**
 * معلومات التسجيل
 */
data class RecordingInfo(
    val duration: Long = 0L,
    val fileSize: Long = 0L,
    val isRecording: Boolean = false
)