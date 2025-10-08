package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver

/**
 * كاشف الشاشات المنبثقة (Overlays)
 * ✅ مُحسّن: كشف سريع للتطبيقات المصغرة
 */
class OverlayDetector(
    private val activity: Activity,
    private val onOverlayDetected: () -> Unit
) {
    private val TAG = "OverlayDetector"
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var isMonitoring = false
    private var lastFocusTime = 0L
    private var focusLossCount = 0

    // ✅ Thresholds مُخفضة للكشف السريع
    private val FOCUS_LOSS_THRESHOLD = 200L  // 200ms فقط
    private val FOCUS_COUNT_THRESHOLD = 1     // مرة واحدة فقط!

    private var focusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener? = null
    private var periodicCheckRunnable: Runnable? = null

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        if (isMonitoring) {
            Log.d(TAG, "Already monitoring")
            return
        }

        isMonitoring = true
        lastFocusTime = System.currentTimeMillis()
        focusLossCount = 0

        // ✅ فحص فوري للـ overlays الموجودة مسبقاً
        handler.postDelayed({
            performInitialOverlayCheck()
        }, 500) // تأخير قصير للسماح للنظام بالاستقرار

        setupWindowFocusMonitoring()
        startPeriodicCheck()

        Log.d(TAG, "✅ Overlay monitoring STARTED")
    }

    /**
     * ✅ فحص أولي للكشف عن overlays موجودة مسبقاً
     */
    private fun performInitialOverlayCheck() {
        if (!isMonitoring) return

        Log.d(TAG, "🔍 Performing initial overlay check...")

        val hasFocus = activity.hasWindowFocus()

        if (!hasFocus) {
            Log.e(TAG, "🚨 INITIAL CHECK: Overlay detected at startup!")

            // تأكيد مرة أخرى بعد فترة قصيرة
            handler.postDelayed({
                if (isMonitoring && !activity.hasWindowFocus()) {
                    Log.e(TAG, "🚨 INITIAL CHECK CONFIRMED: Overlay present before exam start!")
                    handleOverlayDetected("INITIAL_CHECK_OVERLAY_EXISTS")
                } else {
                    Log.d(TAG, "✅ Initial check: Focus OK")
                }
            }, 500)
        } else {
            Log.d(TAG, "✅ Initial check passed: No overlay detected")
        }
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        if (!isMonitoring) return

        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
        periodicCheckRunnable = null

        // إزالة Focus Listener
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

    /**
     * مراقبة Window Focus
     */
    private fun setupWindowFocusMonitoring() {
        focusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (!isMonitoring) return@OnWindowFocusChangeListener

            val now = System.currentTimeMillis()

            if (!hasFocus) {
                val timeSinceLastFocus = now - lastFocusTime

                Log.w(TAG, "⚠️ FOCUS LOST! Time since last focus: ${timeSinceLastFocus}ms")

                // ✅ كشف فوري للتطبيقات المصغرة
                if (timeSinceLastFocus > FOCUS_LOSS_THRESHOLD) {
                    focusLossCount++

                    Log.e(TAG, "🚨 Focus loss count: $focusLossCount/$FOCUS_COUNT_THRESHOLD")

                    if (focusLossCount >= FOCUS_COUNT_THRESHOLD) {
                        // ✅ تأخير قصير جداً للتحقق النهائي
                        handler.postDelayed({
                            if (isMonitoring && !activity.hasWindowFocus()) {
                                Log.e(TAG, "🚨🚨🚨 OVERLAY CONFIRMED - TRIGGERING CALLBACK")
                                handleOverlayDetected("WINDOW_FOCUS_LOST")
                            } else {
                                Log.d(TAG, "✅ Focus restored - False alarm")
                                focusLossCount = 0
                            }
                        }, 300) // 300ms فقط
                    }
                } else {
                    Log.d(TAG, "🟡 Brief focus loss ignored (${timeSinceLastFocus}ms)")
                }
            } else {
                Log.d(TAG, "✅ Focus RESTORED")
                lastFocusTime = now

                // ✅ Reset counter بعد تأخير قصير
                handler.postDelayed({
                    if (isMonitoring && focusLossCount > 0) {
                        Log.d(TAG, "🔄 Resetting focus count (was: $focusLossCount)")
                        focusLossCount = 0
                    }
                }, 1000)
            }
        }

        try {
            activity.window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(focusChangeListener)
            Log.d(TAG, "✅ Focus listener registered")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error adding focus listener", e)
        }
    }

    /**
     * ✅ فحص دوري كل ثانية واحدة
     */
    private fun startPeriodicCheck() {
        periodicCheckRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return

                val hasFocus = activity.hasWindowFocus()

                if (!hasFocus) {
                    Log.w(TAG, "🔍 Periodic check: NO FOCUS - Possible overlay!")

                    // تأكيد إضافي
                    handler.postDelayed({
                        if (isMonitoring && !activity.hasWindowFocus()) {
                            Log.e(TAG, "🚨 PERIODIC CHECK CONFIRMED OVERLAY")
                            handleOverlayDetected("PERIODIC_CHECK_NO_FOCUS")
                        }
                    }, 200)
                } else {
                    Log.d(TAG, "🔍 Periodic check: Focus OK")
                }

                // إعادة الجدولة كل ثانية
                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(periodicCheckRunnable!!, 1000)
        Log.d(TAG, "✅ Periodic check started (every 1 second)")
    }

    /**
     * معالجة اكتشاف Overlay
     */
    private fun handleOverlayDetected(reason: String) {
        if (!isMonitoring) return

        Log.e(TAG, """
            ╔════════════════════════════════════════╗
            ║  🚨 OVERLAY DETECTED: $reason
            ║  Time: ${System.currentTimeMillis()}
            ╚════════════════════════════════════════╝
        """.trimIndent())

        try {
            onOverlayDetected()
            Log.d(TAG, "✅ Overlay callback executed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calling overlay callback", e)
        }

        // إيقاف المراقبة
        handler.postDelayed({
            stopMonitoring()
        }, 100)
    }

    /**
     * استدعاء يدوي من ExamActivity
     */
    fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!isMonitoring) return

        val now = System.currentTimeMillis()

        if (!hasFocus) {
            val timeSinceLastFocus = now - lastFocusTime

            Log.w(TAG, "📱 Manual check: FOCUS LOST (${timeSinceLastFocus}ms since last)")

            if (timeSinceLastFocus > FOCUS_LOSS_THRESHOLD) {
                focusLossCount++

                Log.w(TAG, "📱 Manual focus loss count: $focusLossCount/$FOCUS_COUNT_THRESHOLD")

                if (focusLossCount >= FOCUS_COUNT_THRESHOLD) {
                    handler.postDelayed({
                        if (isMonitoring && !activity.hasWindowFocus()) {
                            Log.e(TAG, "🚨 MANUAL CHECK CONFIRMED OVERLAY")
                            handleOverlayDetected("MANUAL_FOCUS_LOST")
                        }
                    }, 300)
                }
            }
        } else {
            Log.d(TAG, "📱 Manual check: Focus RESTORED")
            lastFocusTime = now

            handler.postDelayed({
                if (isMonitoring) {
                    focusLossCount = 0
                }
            }, 1000)
        }
    }

    /**
     * فحص يدوي للـ Overlays النشطة
     */
    fun checkForActiveOverlays(): Boolean {
        return try {
            val hasFocus = activity.hasWindowFocus()

            if (!hasFocus) {
                Log.w(TAG, "🔍 Active check: NO FOCUS - Overlay detected!")
                true
            } else {
                Log.d(TAG, "🔍 Active check: Focus OK")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for overlays", e)
            false
        }
    }
}