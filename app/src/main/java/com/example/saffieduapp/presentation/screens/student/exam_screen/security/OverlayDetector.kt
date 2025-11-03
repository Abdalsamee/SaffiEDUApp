package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver

/**
 * OverlayDetector
 * كاشف الشاشات المنبثقة (Overlays)
 * - نسخة أكثر استقرارًا وأقل حساسية
 * - يدعم الكتم المؤقت (suppress) أثناء الحوارات والانتقالات
 * - متوافق مع ExamSecurityManager الجديد
 */
class OverlayDetector(
    private val activity: Activity,
    private val onOverlayDetected: () -> Unit,
    /** يستدعى قبل أي فحص لتحديد ما إذا كان الكشف يجب كتمه مؤقتًا */
    private val shouldSuppress: () -> Boolean = { false }
) {
    private val TAG = "OverlayDetector"
    private val handler = Handler(Looper.getMainLooper())

    // ========================= الحالة العامة =========================
    @Volatile private var isMonitoring = false
    private var lastFocusTime = 0L
    private var focusLossCount = 0

    // ========================= إعدادات الحساسية =========================
    private val FOCUS_LOSS_THRESHOLD = 800L   // الوقت الأدنى لفقد الفوكس (ms)
    private val FOCUS_COUNT_THRESHOLD = 2     // عدد مرات الفقد قبل الإنذار

    // ========================= حالات الكتم =========================
    @Volatile private var suppressUntil = 0L          // كتم مؤقت بسبب ديالوجات داخلية
    @Volatile private var resumeSuppressUntil = 0L    // كتم بعد العودة من الخلفية
    @Volatile private var suppressedFlag = false      // كتم يدوي عام

    fun setSuppressed(value: Boolean) { suppressedFlag = value }

    private var focusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener? = null
    private var periodicCheckRunnable: Runnable? = null

    // ==========================================================
    // 🔹 تشغيل وإيقاف المراقبة
    // ==========================================================
    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        lastFocusTime = System.currentTimeMillis()
        focusLossCount = 0

        handler.postDelayed({ performInitialOverlayCheck() }, 600)
        setupWindowFocusMonitoring()
        startPeriodicCheck()
        Log.d(TAG, "✅ Overlay monitoring STARTED")
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
        periodicCheckRunnable = null
        focusChangeListener?.let { listener ->
            try {
                activity.window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing focus listener", e)
            }
        }
        focusChangeListener = null
        Log.d(TAG, "❌ Overlay monitoring STOPPED")
    }

    // ==========================================================
    // 🔹 فحص أولي بعد بدء المراقبة
    // ==========================================================
    private fun performInitialOverlayCheck() {
        if (!isMonitoring || isSuppressedNow()) return

        if (!activity.hasWindowFocus()) {
            handler.postDelayed({
                if (isMonitoring && !activity.hasWindowFocus() && !isSuppressedNow()) {
                    handleOverlayDetected("INITIAL_FOCUS_LOSS")
                }
            }, 700)
        }
    }

    // ==========================================================
    // 🔹 مراقبة تغيّر الفوكس للنافذة
    // ==========================================================
    private fun setupWindowFocusMonitoring() {
        focusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (!isMonitoring || isSuppressedNow()) return@OnWindowFocusChangeListener

            val now = System.currentTimeMillis()
            if (!hasFocus) {
                val delta = now - lastFocusTime
                if (delta > FOCUS_LOSS_THRESHOLD) {
                    focusLossCount++
                    Log.w(TAG, "⚠️ Focus lost #$focusLossCount (>${FOCUS_LOSS_THRESHOLD}ms)")
                    if (focusLossCount >= FOCUS_COUNT_THRESHOLD) {
                        handler.postDelayed({
                            if (isMonitoring && !activity.hasWindowFocus() && !isSuppressedNow()) {
                                handleOverlayDetected("WINDOW_FOCUS_LOST")
                            } else {
                                focusLossCount = 0
                            }
                        }, 400)
                    }
                }
            } else {
                lastFocusTime = now
                handler.postDelayed({ focusLossCount = 0 }, 1000)
            }
        }

        try {
            activity.window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(focusChangeListener)
            Log.d(TAG, "✅ Focus listener registered")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error adding focus listener", e)
        }
    }

    // ==========================================================
    // 🔹 فحص دوري مستمر (كل 1.5 ثانية)
    // ==========================================================
    private fun startPeriodicCheck() {
        periodicCheckRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return

                if (!isSuppressedNow()) {
                    if (!activity.hasWindowFocus()) {
                        handler.postDelayed({
                            if (isMonitoring && !activity.hasWindowFocus() && !isSuppressedNow()) {
                                handleOverlayDetected("PERIODIC_NO_FOCUS")
                            }
                        }, 300)
                    }
                }

                handler.postDelayed(this, 1500)
            }
        }

        handler.postDelayed(periodicCheckRunnable!!, 1500)
        Log.d(TAG, "✅ Periodic check started (every 1.5s)")
    }

    // ==========================================================
    // 🔹 منطق الكشف
    // ==========================================================
    private fun handleOverlayDetected(reason: String) {
        if (!isMonitoring || isSuppressedNow()) return

        Log.e(TAG, "🚨 OVERLAY DETECTED: $reason")
        try {
            onOverlayDetected()
            Log.d(TAG, "✅ Overlay callback executed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in overlay callback", e)
        }
    }

    // ==========================================================
    // 🔹 منطق الكتم (suppress)
    // ==========================================================
    fun suppressFor(ms: Long) {
        suppressUntil = System.currentTimeMillis() + ms
        Log.d(TAG, "⏸️ Suppressing overlay for $ms ms")
    }

    fun suppressAfterResume(durationMs: Long = 2000L) {
        resumeSuppressUntil = System.currentTimeMillis() + durationMs
        Log.d(TAG, "🕒 Suppressing overlay detection for ${durationMs}ms after resume")
    }

    private fun isSuppressedNow(): Boolean {
        val now = System.currentTimeMillis()
        val internal = now < suppressUntil
        val afterResume = now < resumeSuppressUntil
        val external = shouldSuppress()
        return internal || afterResume || external || suppressedFlag
    }

    // ==========================================================
    // 🔹 تفاعل يدوي من ExamActivity
    // ==========================================================
    fun onWindowFocusChanged(hasFocus: Boolean) {
        // يُترك فارغًا — التفاعل يُدار من داخل المستمعين
    }
}
