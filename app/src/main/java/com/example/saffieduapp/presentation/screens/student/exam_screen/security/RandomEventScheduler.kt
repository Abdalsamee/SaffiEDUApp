package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.FrontCameraSnapshotManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.random.Random

/**
 * جدولة الأحداث العشوائية أثناء الاختبار:
 *  - لقطة أمامية عشوائية (تستخدم requestRandomCapture)
 *  - تسجيل فيديو خلفي مرة واحدة عشوائيًا (مع إيقاف كشف الوجه مؤقتًا واستئنافه لاحقًا)
 */
class RandomEventScheduler(
    private val frontSnapshotManager: FrontCameraSnapshotManager,
    private val backCameraRecorder: BackCameraVideoRecorder,
    private val sessionManager: ExamSessionManager,
    private val lifecycleOwner: LifecycleOwner,
    // تم تمرير دوال الإيقاف/الاستئناف من ExamActivity عبر CameraMonitor
    private val pauseFrontDetection: () -> Unit,
    private val resumeFrontDetection: () -> Unit,
) {
    private val TAG = "RandomEventScheduler"
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // اضبط هذه القيم مؤقتًا صغيرة أثناء الاختبار، ثم أعدها لقيم الإنتاج
    private val SNAPSHOT_MIN_DELAY_MS = 15_000L   // 15 ثانية
    private val SNAPSHOT_JITTER_MS    = 10_000L   // ±10 ثواني

    private val VIDEO_MIN_DELAY_MS    = 30_000L   // 30 ثانية
    private val VIDEO_JITTER_MS       = 20_000L   // ±20 ثانية
    private val VIDEO_MAX_DURATION_MS = 10_000L   // نسجل 10 ثوانٍ فقط (BackCameraVideoRecorder يوقف تلقائيًا إن ضبطته)

    @Volatile private var running = false
    @Volatile private var videoScheduled = false

    fun startRandomEvents() {
        if (running) return
        running = true

        // حلقة اللقطات الأمامية العشوائية
        scope.launch {
            while (running) {
                val delayMs = randomDelay(SNAPSHOT_MIN_DELAY_MS, SNAPSHOT_JITTER_MS)
                Log.d(TAG, "🎲 Next random snapshot in ${delayMs}ms")
                delay(delayMs)

                if (!running) break
                if (!sessionManager.canCaptureMoreSnapshots()) {
                    Log.w(TAG, "📸 Max snapshots reached — skipping random capture")
                    continue
                }

                // اطلب لقطة عشوائية من أول فريم يمر على الـ analyzer
                frontSnapshotManager.requestRandomCapture()
            }
        }

        // جدولة فيديو خلفي مرة واحدة فقط
        scope.launch {
            if (videoScheduled) return@launch
            videoScheduled = true

            val delayMs = randomDelay(VIDEO_MIN_DELAY_MS, VIDEO_JITTER_MS)
            Log.d(TAG, "🎲 Back video will start in ${delayMs}ms (one-time)")
            delay(delayMs)

            if (!running) return@launch

            val sessionId = sessionManager.getCurrentSession()?.sessionId
            if (sessionId.isNullOrBlank()) {
                Log.w(TAG, "No active session; skipping back video")
                return@launch
            }

            // 1) أوقف كشف الوجه أثناء التسجيل الخلفي (لتقليل تعارض الكاميرات)
            Log.d(TAG, "⏸️ Pausing front face detection before back recording")
            pauseFrontDetection()

            try {
                // 2) ابدأ التسجيل الخلفي
                Log.d(TAG, "🎥 Starting back room scan (random)")
                val startResult = backCameraRecorder.startRoomScan(lifecycleOwner, sessionId)

                // 3) راقب حالة التسجيل لاستئناف كشف الوجه عند الانتهاء
                launch {
                    backCameraRecorder.recordingState.collectLatest { state ->
                        when (state) {
                            is RecordingState.COMPLETED,
                            RecordingState.STOPPED -> {
                                Log.d(TAG, "✅ Back recording finished — resuming front detection")
                                resumeFrontDetection()
                                this.cancel() // نُنهي المُراقبة لهذا التسجيل
                            }
                            is RecordingState.ERROR -> {
                                Log.e(TAG, "❌ Back recording error: ${state.message} — resuming front detection")
                                resumeFrontDetection()
                                this.cancel()
                            }
                            else -> Unit
                        }
                    }
                }

                // حماية: في حال لم يغلق تلقائيًا لأي سبب، أوقفه يدويًا بعد VIDEO_MAX_DURATION_MS
                launch {
                    delay(VIDEO_MAX_DURATION_MS + 2_000)
                    Log.d(TAG, "⏱️ Safety stop for back recording (if still running)")
                    backCameraRecorder.stopRecording()
                }

                if (startResult.isFailure) {
                    // فشل البدء — استأنف فورًا
                    Log.e(TAG, "Failed to start back recording: ${startResult.exceptionOrNull()?.message}")
                    resumeFrontDetection()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while starting back recording", e)
                resumeFrontDetection()
            }
        }
    }

    fun stop() {
        running = false
        scope.cancel()
    }

    private fun randomDelay(min: Long, jitter: Long): Long {
        val delta = Random.nextLong(-jitter, jitter)
        return (min + delta).coerceAtLeast(1_000L)
    }
}
