package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver

/**
 * كاشف الشاشات المنبثقة (Overlays) - نسخة أقل حساسية + دعم الكتم المؤقت
 */
class OverlayDetector(
    private val activity: Activity,
    private val onOverlayDetected: () -> Unit,
    /** يُستدعى قبل اتخاذ أي إجراء: لو رجّع true نكتم الكشف (مثلاً وقت الديالوجات الداخلية) */
    private val shouldSuppress: () -> Boolean = { false }
) {
    private val TAG = "OverlayDetector"
    private val handler = Handler(Looper.getMainLooper())

    @Volatile private var isMonitoring = false
    private var lastFocusTime = 0L
    private var focusLossCount = 0

    // 🔧 أقل حساسية + إعادة تأكيد أقوى
    private val FOCUS_LOSS_THRESHOLD = 800L   // كان 200ms → الآن 800ms
    private val FOCUS_COUNT_THRESHOLD = 2     // كان 1 → الآن 2 (مرتين)

    private var focusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener? = null
    private var periodicCheckRunnable: Runnable? = null

    // كتم مؤقت بعد تسجيل أي ديالوج داخلي (نتجنب race على الفوكس)
    @Volatile private var suppressUntil = 0L
    fun suppressFor(ms: Long) {
        suppressUntil = System.currentTimeMillis() + ms
    }
    private fun isSuppressedNow(): Boolean {
        val now = System.currentTimeMillis()
        return now < suppressUntil || shouldSuppress()
    }

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        lastFocusTime = System.currentTimeMillis()
        focusLossCount = 0

        // فحص أولي
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
            try { activity.window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(listener) }
            catch (e: Exception) { Log.e(TAG, "Error removing focus listener", e) }
        }
        focusChangeListener = null
        Log.d(TAG, "❌ Overlay monitoring STOPPED")
    }

    private fun performInitialOverlayCheck() {
        if (!isMonitoring) return
        if (isSuppressedNow()) return

        val hasFocus = activity.hasWindowFocus()
        if (!hasFocus) {
            // أعد التأكيد بعد 700ms
            handler.postDelayed({
                if (isMonitoring && !activity.hasWindowFocus() && !isSuppressedNow()) {
                    Log.e(TAG, "🚨 INITIAL CHECK CONFIRMED: Overlay-like focus loss at startup")
                    handleOverlayDetected("INITIAL_FOCUS_LOSS")
                }
            }, 700)
        }
    }

    private fun setupWindowFocusMonitoring() {
        focusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (!isMonitoring) return@OnWindowFocusChangeListener
            if (isSuppressedNow()) return@OnWindowFocusChangeListener

            val now = System.currentTimeMillis()
            if (!hasFocus) {
                val delta = now - lastFocusTime
                if (delta > FOCUS_LOSS_THRESHOLD) {
                    focusLossCount++
                    Log.w(TAG, "⚠️ Focus lost #$focusLossCount (>${FOCUS_LOSS_THRESHOLD}ms)")
                    if (focusLossCount >= FOCUS_COUNT_THRESHOLD) {
                        // تأكيد أخير
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
                // reset بأمان بعد 1s
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

    private fun startPeriodicCheck() {
        periodicCheckRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return
                if (!isSuppressedNow()) {
                    val hasFocus = activity.hasWindowFocus()
                    if (!hasFocus) {
                        // إعادة تأكيد سريعة
                        handler.postDelayed({
                            if (isMonitoring && !activity.hasWindowFocus() && !isSuppressedNow()) {
                                handleOverlayDetected("PERIODIC_NO_FOCUS")
                            }
                        }, 300)
                    }
                }
                handler.postDelayed(this, 1500) // كان 1000 → الآن 1500 لتقليل الحساسية
            }
        }
        handler.postDelayed(periodicCheckRunnable!!, 1500)
        Log.d(TAG, "✅ Periodic check started (every 1.5s)")
    }

    private fun handleOverlayDetected(reason: String) {
        if (!isMonitoring) return
        if (isSuppressedNow()) return

        Log.e(TAG, "🚨 OVERLAY DETECTED: $reason @${System.currentTimeMillis()}")
        try {
            onOverlayDetected()
            Log.d(TAG, "✅ Overlay callback executed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calling overlay callback", e)
        }
        // لا نوقف المراقبة هنا تلقائيًا — القرار للمُستدعي
    }

    /** يمكن استدعاؤه من Activity لتمرير تغيّر الفوكس يدويًا */
    fun onWindowFocusChanged(hasFocus: Boolean) {
        // نتركه بدون منطق إضافي — كل شيء مُدار عبر المستمعين والحُمايات أعلاه
    }
}
