package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.FrontCameraSnapshotManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.SecurityEventType
import kotlinx.coroutines.*

/**
 * RandomEventScheduler
 * =====================
 * مسؤول عن تشغيل الأحداث العشوائية أثناء الامتحان:
 * ✅ التقاط صور عشوائية بالكاميرا الأمامية
 * ✅ تسجيل فيديو قصير بالكاميرا الخلفية مرة واحدة فقط
 * ✅ يعمل بشكل مستقل ولا يؤثر على المراقبة الأمنية المباشرة
 */
class RandomEventScheduler(
    private val frontSnapshotManager: FrontCameraSnapshotManager,
    private val backCameraRecorder: BackCameraVideoRecorder,
    private val sessionManager: ExamSessionManager,
    private val lifecycleOwner: LifecycleOwner
) {
    private val TAG = "RandomEventScheduler"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var hasRecordedRandomVideo = false
    private var isRunning = false

    companion object {
        private const val MAX_RANDOM_SNAPSHOTS = 10
        private const val MIN_SNAPSHOT_DELAY = 45_000L   // 45 ثانية
        private const val MAX_SNAPSHOT_DELAY = 150_000L  // 2.5 دقيقة
        private const val MIN_VIDEO_DELAY = 90_000L      // 1.5 دقيقة
        private const val MAX_VIDEO_DELAY = 240_000L     // 4 دقائق
    }

    /** بدء نظام المراقبة العشوائية **/
    fun startRandomEvents() {
        if (isRunning) return
        isRunning = true

        Log.d(TAG, "🎯 Random event scheduler started")
        scheduleRandomSnapshots()
        scheduleRandomBackCameraRecording()
    }

    /** 🔹 جدولة التقاط صور أمامية عشوائية **/
    private fun scheduleRandomSnapshots() {
        repeat(MAX_RANDOM_SNAPSHOTS) { index ->
            val delayMs = (MIN_SNAPSHOT_DELAY..MAX_SNAPSHOT_DELAY).random()
            scope.launch {
                delay(delayMs * (index + 1)) // فاصل زمني متغير
                try {
                    if (sessionManager.canCaptureMoreSnapshots()) {
                        frontSnapshotManager.captureRandomSnapshot("RANDOM_SNAPSHOT_${index + 1}")
                        Log.d(TAG, "📸 Random snapshot captured #${index + 1}")
                    } else {
                        Log.w(TAG, "⚠️ Max snapshots reached before #${index + 1}")
                        cancel()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to capture random snapshot #${index + 1}", e)
                }
            }
        }
    }

    /** 🔹 جدولة تسجيل فيديو خلفي عشوائي واحد **/
    private fun scheduleRandomBackCameraRecording() {
        val randomDelay = (MIN_VIDEO_DELAY..MAX_VIDEO_DELAY).random()

        scope.launch {
            delay(randomDelay)
            if (!hasRecordedRandomVideo) {
                hasRecordedRandomVideo = true
                try {
                    val sessionId = sessionManager.getCurrentSession()?.sessionId ?: ""
                    if (sessionId.isEmpty()) {
                        Log.e(TAG, "❌ No active session ID for random video")
                        return@launch
                    }

                    Log.d(TAG, "🎥 Starting random back camera video...")
                    val result = backCameraRecorder.startRoomScan(lifecycleOwner, sessionId)

                    if (result.isSuccess) {
                        Log.d(TAG, "✅ Random back camera video recorded successfully")
                        sessionManager.logSecurityEvent(
                            type = SecurityEventType.ROOM_SCAN_COMPLETED,
                            details = "Random back camera scan triggered automatically"
                        )
                    } else {
                        Log.e(TAG, "❌ Failed to record random back camera video: ${result.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception during random back video", e)
                }
            }
        }
    }

    /** إيقاف النظام **/
    fun stop() {
        scope.cancel()
        isRunning = false
        Log.d(TAG, "🛑 Random event scheduler stopped")
    }

    /** تنظيف الموارد **/
    fun cleanup() {
        stop()
        Log.d(TAG, "🧹 Random event scheduler cleaned up")
    }
}
