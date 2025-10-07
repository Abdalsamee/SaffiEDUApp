package com.example.saffieduapp.presentation.screens.student.exam_screen.camera.back

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.CameraMonitoringConfig
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.models.BackCameraRecording
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.models.VideoMetadata
import com.example.saffieduapp.presentation.screens.student.exam_screen.camera.storage.EncryptedMediaStorage
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.Date
import java.util.UUID
import java.util.concurrent.Executors

/**
 * 📹 مسجل الكاميرا الخلفية
 *
 * 📍 المسار:
 * app/src/main/java/com/example/saffieduapp/presentation/screens/student/exam_screen/camera/back/BackCameraRecorder.kt
 *
 * 🎯 الهدف:
 * تسجيل فيديو 10 ثواني من الكاميرا الخلفية لمسح الغرفة
 *
 * 📊 المواصفات:
 * - الدقة: 854x480 (SD)
 * - المدة: 10 ثواني
 * - الحجم المستهدف: 1.5-2 MB
 * - التشفير: تلقائي بعد التسجيل
 */
class BackCameraRecorder(
    private val context: Context,
    private val storage: EncryptedMediaStorage,
    private val sessionId: String,
    private val examId: String,
    private val studentId: String
) {
    private val TAG = "BackCameraRecorder"

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val executor = Executors.newSingleThreadExecutor()

    // مكونات CameraX
    private var videoCapture: VideoCapture<Recorder>? = null
    private var preview: androidx.camera.core.Preview? = null  // إضافة Preview
    private var activeRecording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null

    // حالة التسجيل
    private val _state = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    // معلومات التسجيل
    private var recordingStartTime: Long = 0
    private var tempVideoFile: File? = null

    // ═══════════════════════════════════════════
    // 📊 حالات التسجيل
    // ═══════════════════════════════════════════

    sealed class RecordingState {
        data object Idle : RecordingState()
        data object Initializing : RecordingState()
        data object Ready : RecordingState()
        data class Recording(val elapsedSeconds: Int) : RecordingState()
        data object Processing : RecordingState()
        data class Completed(val recording: BackCameraRecording) : RecordingState()
        data class Error(val message: String) : RecordingState()
    }

    // ═══════════════════════════════════════════
    // 🏗️ التهيئة
    // ═══════════════════════════════════════════

    /**
     * تهيئة الكاميرا
     */
    suspend fun initialize(lifecycleOwner: LifecycleOwner, previewView: androidx.camera.view.PreviewView): Result<Unit> {
        return withContext(Dispatchers.Main) {
            try {
                _state.value = RecordingState.Initializing

                if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                    Log.d(TAG, "🎥 Initializing back camera...")
                }

                // الحصول على CameraProvider
                val providerFuture = ProcessCameraProvider.getInstance(context)
                cameraProvider = providerFuture.get()

                // إعداد VideoCapture
                setupVideoCapture()

                // إعداد Preview
                setupPreview(previewView)

                // ربط الكاميرا
                bindCamera(lifecycleOwner)

                _state.value = RecordingState.Ready

                if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                    Log.d(TAG, "✅ Back camera initialized")
                }

                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to initialize camera", e)
                _state.value = RecordingState.Error("فشل تهيئة الكاميرا: ${e.message}")
                Result.failure(e)
            }
        }
    }

    /**
     * إعداد VideoCapture بالمواصفات المطلوبة
     */
    private fun setupVideoCapture() {
        // إنشاء Recorder
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.SD, // 480p
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            )
            .setExecutor(executor)
            .build()

        // إنشاء VideoCapture
        videoCapture = VideoCapture.withOutput(recorder)

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "📹 VideoCapture configured:")
            Log.d(TAG, "   Quality: SD (480p)")
            Log.d(TAG, "   Target size: ${CameraMonitoringConfig.BackCamera.TARGET_WIDTH}x${CameraMonitoringConfig.BackCamera.TARGET_HEIGHT}")
        }
    }

    /**
     * إعداد Preview
     */
    private fun setupPreview(previewView: androidx.camera.view.PreviewView) {
        preview = androidx.camera.core.Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "📺 Preview configured")
        }
    }

    /**
     * ربط الكاميرا الخلفية
     */
    private fun bindCamera(lifecycleOwner: LifecycleOwner) {
        val provider = cameraProvider ?: throw IllegalStateException("CameraProvider not initialized")

        // إلغاء أي ربط سابق
        provider.unbindAll()

        // اختيار الكاميرا الخلفية
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // ربط Preview + VideoCapture
        provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,  // إضافة Preview
            videoCapture
        )

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "📷 Back camera bound to lifecycle (with preview)")
        }
    }

    // ═══════════════════════════════════════════
    // ⏺️ التسجيل
    // ═══════════════════════════════════════════

    /**
     * بدء التسجيل
     */
    fun startRecording() {
        if (_state.value !is RecordingState.Ready) {
            Log.w(TAG, "⚠️ Cannot start recording - Current state: ${_state.value}")
            return
        }

        scope.launch {
            try {
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                Log.d(TAG, "🎬 Starting recording...")
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

                // إنشاء ملف مؤقت للتسجيل
                tempVideoFile = File(
                    context.cacheDir,
                    "recording_${System.currentTimeMillis()}.mp4"
                )

                Log.d(TAG, "📁 Temp file: ${tempVideoFile!!.absolutePath}")

                // إعداد OutputOptions
                val outputOptions = FileOutputOptions.Builder(tempVideoFile!!)
                    .build()

                // بدء التسجيل
                activeRecording = videoCapture?.output
                    ?.prepareRecording(context, outputOptions)
                    ?.apply {
                        // تمكين الصوت إذا كان متاحاً
                        if (ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            withAudioEnabled()
                            Log.d(TAG, "🎤 Audio enabled")
                        } else {
                            Log.w(TAG, "⚠️ Audio permission not granted")
                        }
                    }
                    ?.start(executor) { event ->
                        handleRecordingEvent(event)
                    }

                if (activeRecording == null) {
                    throw Exception("Failed to start recording - activeRecording is null")
                }

                recordingStartTime = System.currentTimeMillis()
                _state.value = RecordingState.Recording(0)

                Log.d(TAG, "✅ Recording started successfully!")

                // بدء العد التنازلي (10 ثواني)
                startRecordingTimer()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error starting recording", e)
                Log.e(TAG, "   VideoCapture: ${videoCapture != null}")
                Log.e(TAG, "   Error: ${e.message}")
                _state.value = RecordingState.Error("فشل بدء التسجيل: ${e.message}")
            }
        }
    }

    /**
     * معالجة أحداث التسجيل
     */
    private fun handleRecordingEvent(event: VideoRecordEvent) {
        when (event) {
            is VideoRecordEvent.Start -> {
                if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                    Log.d(TAG, "▶️ Recording started")
                }
            }

            is VideoRecordEvent.Status -> {
                // تحديث التقدم
                val elapsed = (System.currentTimeMillis() - recordingStartTime) / 1000
                _state.value = RecordingState.Recording(elapsed.toInt())
            }

            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    Log.e(TAG, "❌ Recording error: ${event.error}")
                    _state.value = RecordingState.Error("خطأ في التسجيل")
                } else {
                    if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                        Log.d(TAG, "✅ Recording finalized")
                    }
                    processRecording()
                }
            }
        }
    }

    /**
     * بدء مؤقت التسجيل (10 ثواني)
     */
    private fun startRecordingTimer() {
        scope.launch {
            delay(CameraMonitoringConfig.BackCamera.RECORDING_DURATION)
            stopRecording()
        }
    }

    /**
     * إيقاف التسجيل
     */
    private fun stopRecording() {
        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "⏹️ Stopping recording...")
        }

        activeRecording?.stop()
        activeRecording = null
    }

    // ═══════════════════════════════════════════
    // 🔄 معالجة الفيديو
    // ═══════════════════════════════════════════

    /**
     * معالجة الفيديو بعد التسجيل
     */
    private fun processRecording() {
        scope.launch(Dispatchers.IO) {
            try {
                _state.value = RecordingState.Processing

                if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                    Log.d(TAG, "🔄 Processing video...")
                }

                val videoFile = tempVideoFile ?: throw IllegalStateException("No video file")

                // 1️⃣ التحقق من حجم الملف
                val fileSize = videoFile.length()
                if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                    Log.d(TAG, "📊 Video size: ${fileSize / 1024} KB")
                }

                if (fileSize > CameraMonitoringConfig.BackCamera.MAX_FILE_SIZE) {
                    Log.w(TAG, "⚠️ Video too large, compression needed")
                    // TODO: ضغط الفيديو
                }

                // 2️⃣ تشفير الفيديو
                val encryptResult = storage.encryptVideo(videoFile)
                if (encryptResult.isFailure) {
                    throw Exception("فشل تشفير الفيديو")
                }

                val encryptedFile = encryptResult.getOrNull()!!

                if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                    Log.d(TAG, "🔐 Video encrypted: ${encryptedFile.name}")
                }

                // 3️⃣ إنشاء معلومات التسجيل
                val recording = BackCameraRecording(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    examId = examId,
                    studentId = studentId,
                    filePath = videoFile.absolutePath,
                    encryptedFilePath = encryptedFile.absolutePath,
                    fileSize = encryptedFile.length(),
                    duration = CameraMonitoringConfig.BackCamera.RECORDING_DURATION,
                    recordedAt = Date(),
                    scheduledAt = 0, // سيتم ملؤها من المجدول
                    metadata = VideoMetadata(
                        width = CameraMonitoringConfig.BackCamera.TARGET_WIDTH,
                        height = CameraMonitoringConfig.BackCamera.TARGET_HEIGHT,
                        bitrate = CameraMonitoringConfig.BackCamera.TARGET_BITRATE,
                        fps = CameraMonitoringConfig.BackCamera.TARGET_FPS,
                        codec = CameraMonitoringConfig.BackCamera.VIDEO_CODEC,
                        hasAudio = true,
                        deviceModel = android.os.Build.MODEL,
                        deviceOrientation = 0
                    )
                )

                // 4️⃣ حذف الملف المؤقت
                videoFile.delete()

                // 5️⃣ تسجيل في log
                storage.appendLog("Back camera recording completed: ${recording.id}")

                // 6️⃣ تحديث الحالة
                _state.value = RecordingState.Completed(recording)

                if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
                    Log.d(TAG, "✅ Recording processing completed")
                    Log.d(TAG, "   ID: ${recording.id}")
                    Log.d(TAG, "   Size: ${recording.fileSize / 1024} KB")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error processing recording", e)
                _state.value = RecordingState.Error("فشل معالجة الفيديو: ${e.message}")
            }
        }
    }

    // ═══════════════════════════════════════════
    // 🧹 التنظيف
    // ═══════════════════════════════════════════

    /**
     * إعادة التعيين
     */
    fun reset() {
        stopRecording()
        _state.value = RecordingState.Idle
        tempVideoFile?.delete()
        tempVideoFile = null
    }

    /**
     * تنظيف الموارد
     */
    fun cleanup() {
        reset()
        cameraProvider?.unbindAll()
        executor.shutdown()
        scope.cancel()

        if (CameraMonitoringConfig.Debug.DETAILED_LOGGING) {
            Log.d(TAG, "🧹 Recorder cleaned up")
        }
    }
}