package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import androidx.lifecycle.LifecycleOwner
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSessionManager
import com.example.saffieduapp.presentation.screens.student.exam_screen.session.FrontCameraSnapshotManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * يُطلق أحداث عشوائية أثناء الاختبار:
 * 1) Snapshots من الكاميرا الأمامية (حتى 10 كحد أقصى تديرها الجلسة)
 * 2) تسجيل مفاجئ واحد للكاميرا الخلفية (Room Scan) لمدة قصيرة
 */
class RandomEventScheduler(
    private val frontSnapshotManager: FrontCameraSnapshotManager,
    private val backCameraRecorder: BackCameraVideoRecorder,
    private val sessionManager: ExamSessionManager,
    private val lifecycleOwner: LifecycleOwner,
    private val pauseFrontDetection: (() -> Unit)? = null,
    private val resumeFrontDetection: (() -> Unit)? = null
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    @Volatile private var started = false
    @Volatile private var backScanTriggered = false

    fun startRandomEvents() {
        if (started) return
        started = true

        // جدولة اللقطات العشوائية للكاميرا الأمامية
        scope.launch {
            scheduleRandomSnapshots()
        }

        // جدولة مسح الغرفة لمرة واحدة بالكاميرا الخلفية
        scope.launch {
            scheduleOneTimeBackScan()
        }
    }

    fun stop() {
        started = false
        scope.cancel()
    }

    // ---------------------------------------------
    // لقطات أمامية عشوائية
    // ---------------------------------------------
    private suspend fun scheduleRandomSnapshots() {
        // نافذة زمنية عشوائية بين 20 - 50 ثانية بين كل محاولة
        val minDelayMs = 20_000L
        val maxDelayMs = 50_000L

        while (started) {
            // لو وصلنا للحد الأقصى للجلسة؛ نتوقف
            if (!sessionManager.canCaptureMoreSnapshots()) break

            val wait = Random.nextLong(minDelayMs, maxDelayMs)
            delay(wait)

            if (!started) break
            // اطلب لقطة من الإطار التالي المتاح
            frontSnapshotManager.requestRandomCapture()
        }
    }

    // ---------------------------------------------
    // مسح الغرفة الخلفي مرة واحدة
    // ---------------------------------------------
    private suspend fun scheduleOneTimeBackScan() {
        if (backScanTriggered) return
        backScanTriggered = true

        // انتظر وقتًا عشوائيًا قبل بدء المسح (بين 40-90 ثانية مثلاً)
        val delayBeforeStart = Random.nextLong(40_000L, 90_000L)
        delay(delayBeforeStart)
        if (!started) return

        // أوقف كشف الوجوه الأمامي أثناء المسح إن رغبت
        pauseFrontDetection?.invoke()

        // ابدأ التسجيل
        val sessionId = sessionManager.getCurrentSession()?.sessionId
        if (sessionId != null) {
            // نستخدم 10 ثوانٍ كحد أقصى (تأكد أن BackCameraVideoRecorder مضبوط على ذلك)
            try {
                backCameraRecorder.startRoomScan(lifecycleOwner, sessionId)
            } catch (_: Exception) {
                // لو فشل البدء، نُعيد تشغيل المراقبة الأمامية ونخرج
                resumeFrontDetection?.invoke()
                return
            }

            // ✅ بدل while(isActive): انتظر أول حالة نهائية عبر الـ StateFlow
            val terminalState = backCameraRecorder.recordingState.first { state ->
                state is RecordingState.COMPLETED ||
                        state is RecordingState.ERROR ||
                        state is RecordingState.STOPPED
            }

            // بعد الانتهاء، أعد تشغيل كشف الوجوه الأمامي
            resumeFrontDetection?.invoke()
        } else {
            // لا توجد جلسة نشطة
            resumeFrontDetection?.invoke()
        }
    }
}
