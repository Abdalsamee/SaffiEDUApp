package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.FrontCameraSnapshotManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import kotlinx.coroutines.isActive  // مهم لاستخدام isActive داخل launch

/**
 * يشغّل:
 *  - لقطات أمامية عشوائية (requestRandomCapture)
 *  - مسح مفاجئ مرة واحدة بالكاميرا الخلفية (10 ثوانٍ)
 */
class RandomEventScheduler(
    private val frontSnapshotManager: FrontCameraSnapshotManager,
    private val backCameraRecorder: BackCameraVideoRecorder,
    private val sessionManager: ExamSessionManager,
    private val lifecycleOwner: LifecycleOwner,
    private val coverageTracker: CoverageTracker,
    // UI hooks:
    private val onShowRoomScanOverlay: (targetMs: Long) -> Unit,
    private val onUpdateUi: (elapsedMs: Long, targetMs: Long) -> Unit,
    private val onHideRoomScanOverlay: () -> Unit,
    // تحكم في كشف الوجوه الأمامي أثناء المسح:
    private val pauseFrontDetection: () -> Unit,
    private val resumeFrontDetection: () -> Unit
) {
    private val TAG = "RandomEventScheduler"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var roomScanTriggered = false

    fun startRandomEvents() {
        scheduleRandomSnapshots()
        scheduleOneRandomRoomScan()
    }

    fun stop() {
        scope.cancel()
    }

    // ——— لقطات أمامية عشوائية ———
    private fun scheduleRandomSnapshots() = scope.launch {
        val rnd = Random.Default
        while (isActive) {
            val delayMs = rnd.nextLong(45_000L, 75_000L) // بين 45–75 ثانية
            delay(delayMs)
            try {
                frontSnapshotManager.requestRandomCapture()
                Log.d(TAG, "📸 Random snapshot requested")
            } catch (e: Exception) {
                Log.e(TAG, "Error requesting random snapshot", e)
            }
        }
    }

    // ——— مسح مفاجئ مرة واحدة ———
    private fun scheduleOneRandomRoomScan() = scope.launch {
        if (roomScanTriggered) return@launch
        roomScanTriggered = true

        val rndDelay = Random.nextLong(60_000L, 120_000L) // بين دقيقة ودقيقتين
        delay(rndDelay)

        val sessionId = sessionManager.getCurrentSession()?.sessionId ?: run {
            Log.w(TAG, "No sessionId available for room scan")
            return@launch
        }

        val targetMs = 10_000L

        try {
            pauseFrontDetection()
            onShowRoomScanOverlay(targetMs)

            // نبدأ تحديث واجهة الـ Overlay
            val uiJob = launch {
                val start = SystemClock.elapsedRealtime()
                while (isActive) {
                    val elapsed = SystemClock.elapsedRealtime() - start
                    onUpdateUi(elapsed, targetMs)
                    if (elapsed >= targetMs) break
                    delay(200)
                }
            }

            // ابدأ التسجيل الخلفي
            val startRes = backCameraRecorder.startRoomScan(lifecycleOwner, sessionId)
            if (startRes.isFailure) {
                Log.e(TAG, "Failed to start back camera scan: ${startRes.exceptionOrNull()?.message}")
                uiJob.cancel()
                onHideRoomScanOverlay()
                resumeFrontDetection()
                return@launch
            }

            // نضمن التوقف بعد 10 ثواني
            delay(targetMs)
            backCameraRecorder.stopRecording()

            // انتظر حتى يصبح التسجيل Finalized
            waitForFinalize()

            uiJob.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error during random room scan", e)
        } finally {
            onHideRoomScanOverlay()
            resumeFrontDetection()
        }
    }

    private suspend fun waitForFinalize() {
        // ننتظر حالة COMPLETE/ERROR/STOPPED
        while (scope.isActive) {
            val st = backCameraRecorder.recordingState.value
            if (st is RecordingState.COMPLETED || st is RecordingState.ERROR || st is RecordingState.STOPPED) {
                break
            }
            delay(300)
        }
    }
}
