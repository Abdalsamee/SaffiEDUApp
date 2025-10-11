package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.content.Context
import android.os.Environment
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * مسجل فيديو الكاميرا الخلفية - لمسح الغرفة (مع شرط التغطية)
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

    private var monitorJob: Job? = null
    private val isRecording = AtomicBoolean(false)
    private var startElapsedMs: Long = 0L

    companion object {
        // زمن الهدف الناعم (لو تحققت التغطية قبل/بعده نوقف)
        private const val SOFT_DURATION_MS = 10_000L
        // الحد الأقصى الصلب لمنع الإطالة
        private const val HARD_CAP_MS = 15_000L
    }

    /**
     * بدء مسح الغرفة: يتوقف عندما تتحقق التغطية أو نصل للحد الأقصى
     */
    suspend fun startRoomScan(
        lifecycleOwner: LifecycleOwner,
        sessionId: String,
        coverageTracker: RoomScanCoverageTracker? = null,
        softDurationMs: Long = SOFT_DURATION_MS,
        hardCapMs: Long = HARD_CAP_MS
    ): Result<File> {
        return try {
            Log.d(TAG, "Starting room scan...")

            cameraProvider = ProcessCameraProvider.getInstance(context).get()

            val qualitySelector = QualitySelector.from(
                Quality.SD,
                FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
            )

            val recorder = Recorder.Builder()
                .setQualitySelector(qualitySelector)
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

            coverageTracker?.start()

            val videoFile = createVideoFile(sessionId)
            startRecording(videoFile)

            // راقب الزمن + التغطية لتحديد وقت الإيقاف
            startStopMonitor(coverageTracker, softDurationMs, hardCapMs)

            Log.d(TAG, "✅ Room scan started")
            Result.success(videoFile)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start room scan", e)
            _recordingState.value = RecordingState.ERROR(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    private fun startRecording(videoFile: File) {
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        activeRecording = videoCapture?.output
            ?.prepareRecording(context, outputOptions)
            ?.start(ContextCompat.getMainExecutor(context)) { event ->
                handleRecordingEvent(event, videoFile)
            }

        isRecording.set(true)
        _recordingState.value = RecordingState.RECORDING
        startElapsedMs = SystemClock.elapsedRealtime()
        Log.d(TAG, "Recording started: ${videoFile.name}")
    }

    /**
     * يراقب:
     * - لو elapsed ≥ soft && coverage.complete ⇒ أوقف
     * - لو elapsed ≥ hardCap ⇒ أوقف إجباريًا
     */
    private fun startStopMonitor(
        coverageTracker: RoomScanCoverageTracker?,
        softDurationMs: Long,
        hardCapMs: Long
    ) {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            while (isRecording.get()) {
                delay(300)
                val elapsed = SystemClock.elapsedRealtime() - startElapsedMs
                val coverageComplete = coverageTracker?.state?.value?.complete == true

                if ((elapsed >= softDurationMs && coverageComplete) || (elapsed >= hardCapMs)) {
                    Log.d(TAG, "Auto stop condition met (elapsed=$elapsed, coverage=$coverageComplete)")
                    stopRecording()
                    break
                }
            }
        }
    }

    private fun handleRecordingEvent(event: VideoRecordEvent, videoFile: File) {
        when (event) {
            is VideoRecordEvent.Start -> {
                Log.d(TAG, "Recording event: START")
            }
            is VideoRecordEvent.Status -> {
                _recordingDuration.value = event.recordingStats.recordedDurationNanos / 1_000_000
            }
            is VideoRecordEvent.Finalize -> {
                monitorJob?.cancel()
                monitorJob = null
                isRecording.set(false)
                activeRecording = null

                if (event.hasError()) {
                    Log.e(TAG, "Recording error: ${event.error}")
                    _recordingState.value = RecordingState.ERROR(
                        "Recording failed: ${event.error}"
                    )
                } else {
                    Log.d(TAG, "✅ Recording completed: ${videoFile.absolutePath}")
                    _recordingState.value = RecordingState.COMPLETED(videoFile)

                    // احفظ الفيديو في الجلسة
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

    fun stopRecording() {
        if (!isRecording.compareAndSet(true, false)) {
            Log.d(TAG, "stopRecording ignored (not recording)")
            return
        }
        activeRecording?.stop()
        activeRecording = null
        _recordingState.value = RecordingState.STOPPED
        Log.d(TAG, "Recording stopped")
    }

    private fun createVideoFile(sessionId: String): File {
        val videoDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "exam_videos"
        )
        if (!videoDir.exists()) videoDir.mkdirs()
        return File(videoDir, "room_scan_${sessionId}_${System.currentTimeMillis()}.mp4")
    }

    fun cleanup() {
        monitorJob?.cancel()
        monitorJob = null
        stopRecording() // سيتجاهل لو مش شغّال
        cameraProvider?.unbindAll()
        cameraProvider = null
        executor.shutdown()
        Log.d(TAG, "Resources cleaned up")
    }
}

sealed class RecordingState {
    object IDLE : RecordingState()
    object RECORDING : RecordingState()
    object STOPPED : RecordingState()
    data class COMPLETED(val videoFile: File) : RecordingState()
    data class ERROR(val message: String) : RecordingState()
}

data class RecordingInfo(
    val duration: Long = 0L,
    val fileSize: Long = 0L,
    val isRecording: Boolean = false
)
