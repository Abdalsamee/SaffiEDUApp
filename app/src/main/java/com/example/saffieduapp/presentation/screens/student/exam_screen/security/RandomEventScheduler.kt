package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.FrontCameraSnapshotManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.filter

/**
 * جدولة أحداث عشوائية أثناء الامتحان:
 * - لقطة front عشوائية (مرة واحدة)
 * - مسح فيديو بالكاميرا الخلفية (مرة واحدة)
 */
class RandomEventScheduler(
    private val frontSnapshotManager: FrontCameraSnapshotManager,
    private val backCameraRecorder: BackCameraVideoRecorder,
    private val sessionManager: ExamSessionManager,
    private val lifecycleOwner: LifecycleOwner,
    private val pauseFrontDetection: () -> Unit,
    private val resumeFrontDetection: () -> Unit,
    private val onShowRoomScanOverlay: () -> Unit,
    private val onHideRoomScanOverlay: () -> Unit,
    private val coverageTracker: RoomScanCoverageTracker
) {
    private val TAG = "RandomEventScheduler"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // للمراقبة من الواجهة: الزمن الحالي للتسجيل (ms)
    @Volatile private var currentRecordingMs: Long = 0L

    fun getCurrentRecordingMs(): Long = currentRecordingMs

    fun startRandomEvents() {
        scope.launch {
            try {
                // 1) لقطة front بعد تأخير عشوائي 15-45 ثانية
                val delay1 = (15_000L..45_000L).random()
                delay(delay1)
                Log.d(TAG, "Requesting random front snapshot after $delay1 ms")
                frontSnapshotManager.requestRandomCapture()

                // 2) مسح خلفي بعد تأخير إضافي 30-60 ثانية
                val delay2 = (30_000L..60_000L).random()
                delay(delay2)
                Log.d(TAG, "Starting random back-camera scan after extra $delay2 ms")

                val sessionId = sessionManager.getCurrentSession()?.sessionId
                    ?: sessionManager.startSession().sessionId

                // جهّز الواجهة
                onShowRoomScanOverlay.invoke()

                // أوقف كشف الوجوه الأمامي وقت التسجيل الخلفي
                pauseFrontDetection.invoke()

                // ابدأ التسجيل
                currentRecordingMs = 0L
                val result = backCameraRecorder.startRoomScan(lifecycleOwner, sessionId)
                if (result.isFailure) {
                    Log.e(TAG, "Failed to start back-camera scan: ${result.exceptionOrNull()?.message}")
                    onHideRoomScanOverlay.invoke()
                    resumeFrontDetection.invoke()
                    return@launch
                }

                // حدّث المؤقّت أثناء التسجيل
                val durationJob = launch {
                    while (isActive) {
                        currentRecordingMs = backCameraRecorder.recordingDuration.value
                        delay(200)
                    }
                }

                // انتظر نهاية التسجيل (COMPLETED/STOPPED/ERROR)
                backCameraRecorder.recordingState
                    .filter { it is RecordingState.COMPLETED || it is RecordingState.ERROR || it is RecordingState.STOPPED }
                    .first()

                durationJob.cancelAndJoin()

                // أغلق الواجهة وارجع للمراقبة الأمامية
                onHideRoomScanOverlay.invoke()
                resumeFrontDetection.invoke()

            } catch (e: CancellationException) {
                Log.w(TAG, "Random events cancelled")
            } catch (e: Exception) {
                Log.e(TAG, "Error in random events", e)
                // تأكد من إعادة كل شيء لو صار خطأ
                try {
                    onHideRoomScanOverlay.invoke()
                    resumeFrontDetection.invoke()
                } catch (_: Exception) { }
            }
        }
    }

    fun stop() {
        scope.cancel()
        try {
            onHideRoomScanOverlay.invoke()
            resumeFrontDetection.invoke()
        } catch (_: Exception) { }
    }
}
