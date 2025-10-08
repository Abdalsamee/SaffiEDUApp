package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Activity
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.annotation.RequiresApi

/**
 * كاشف الشاشات المنبثقة (Overlays)
 * ✅ محسّن مع تأخير في الكشف لتجنب false positives
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

    // ✅ تأخير في الكشف لتجنب false positives من الـ Dialogs الداخلية
    private val FOCUS_LOSS_THRESHOLD = 800L  // زيادة من 500ms إلى 800ms
    private val FOCUS_COUNT_THRESHOLD = 3     // زيادة من 2 إلى 3 مرات

    // للكشف عن Overlays عبر Touch Events
    private var detectorView: View? = null
    private var windowManager: WindowManager? = null

    private var visibilityCheckRunnable: Runnable? = null
    private var focusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener? = null

    // ✅ تتبع آخر focus loss للتحقق من الفترة الزمنية
    private var lastFocusLossTime = 0L

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        lastFocusTime = System.currentTimeMillis()
        focusLossCount = 0

        // 1. مراقبة Window Focus
        setupWindowFocusMonitoring()

        // 2. إنشاء Detector View (لـ Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setupOverlayDetectorView()
        }

        Log.d(TAG, "✅ Overlay monitoring started")
    }

    /**
     * إيقاف المراقبة
     */
    fun stopMonitoring() {
        if (!isMonitoring) return

        isMonitoring = false
        handler.removeCallbacksAndMessages(null)
        visibilityCheckRunnable = null

        // إزالة Focus Listener
        focusChangeListener?.let { listener ->
            try {
                activity.window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing focus listener", e)
            }
        }
        focusChangeListener = null

        removeDetectorView()
        Log.d(TAG, "❌ Overlay monitoring stopped")
    }

    /**
     * مراقبة Window Focus
     * عندما يفقد التطبيق Focus بسبب Overlay
     */
    private fun setupWindowFocusMonitoring() {
        focusChangeListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (!isMonitoring) return@OnWindowFocusChangeListener

            if (!hasFocus) {
                val now = System.currentTimeMillis()
                val timeSinceLastFocus = now - lastFocusTime
                val timeSinceLastLoss = now - lastFocusLossTime

                // ✅ تجاهل focus losses المتكررة جداً (أقل من 300ms)
                // هذه غالباً من animations أو transitions
                if (timeSinceLastLoss < 300) {
                    Log.d(TAG, "🟡 Rapid focus loss ignored (${timeSinceLastLoss}ms)")
                    return@OnWindowFocusChangeListener
                }

                lastFocusLossTime = now

                // ✅ فقط إذا فقدنا Focus لفترة معقولة نعتبرها مخالفة محتملة
                if (timeSinceLastFocus > FOCUS_LOSS_THRESHOLD) {
                    focusLossCount++
                    Log.w(TAG, "⚠️ Focus lost! Count: $focusLossCount/$FOCUS_COUNT_THRESHOLD, Duration: ${timeSinceLastFocus}ms")

                    // ✅ نحتاج عدة مرات قبل اعتبارها overlay حقيقي
                    if (focusLossCount >= FOCUS_COUNT_THRESHOLD) {
                        // ✅ تأخير إضافي للتأكد
                        handler.postDelayed({
                            if (isMonitoring && !activity.hasWindowFocus()) {
                                handleOverlayDetected("WINDOW_FOCUS_LOST")
                            } else {
                                Log.d(TAG, "✅ Focus restored before violation - CLEARED")
                                focusLossCount = 0
                            }
                        }, 500)
                    }
                } else {
                    Log.d(TAG, "🟢 Brief focus loss ignored (${timeSinceLastFocus}ms)")
                }
            } else {
                lastFocusTime = System.currentTimeMillis()
                // ✅ Reset counter عند استعادة Focus مع تأخير
                handler.postDelayed({
                    if (isMonitoring) {
                        if (focusLossCount > 0) {
                            Log.d(TAG, "🔄 Resetting focus count (was: $focusLossCount)")
                        }
                        focusLossCount = 0
                    }
                }, 2000)
            }
        }

        try {
            activity.window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(focusChangeListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding focus listener", e)
        }
    }

    /**
     * إنشاء View شفاف للكشف عن Touch Blocking
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupOverlayDetectorView() {
        try {
            windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            detectorView = View(activity).apply {
                setBackgroundColor(0x00000000) // شفاف تماماً
            }

            val params = WindowManager.LayoutParams(
                1, 1,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            windowManager?.addView(detectorView, params)

            detectorView?.setOnTouchListener { _, _ ->
                Log.d(TAG, "Touch detected - No overlay blocking")
                false
            }

            startPeriodicVisibilityCheck()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup overlay detector view", e)
        }
    }

    /**
     * فحص دوري للـ Visibility
     */
    private fun startPeriodicVisibilityCheck() {
        visibilityCheckRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return

                detectorView?.let { view ->
                    if (!view.isShown) {
                        Log.w(TAG, "Detector view hidden - possible overlay")
                        handleOverlayDetected("DETECTOR_VIEW_HIDDEN")
                        return
                    }
                }

                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(visibilityCheckRunnable!!, 3000)
    }

    /**
     * إزالة Detector View
     */
    private fun removeDetectorView() {
        try {
            detectorView?.let { view ->
                if (view.isAttachedToWindow) {
                    windowManager?.removeView(view)
                }
            }
            detectorView = null
            windowManager = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove detector view", e)
        }
    }

    /**
     * معالجة اكتشاف Overlay
     */
    private fun handleOverlayDetected(reason: String) {
        if (!isMonitoring) return

        Log.e(TAG, "🚨 OVERLAY DETECTED: $reason")

        try {
            onOverlayDetected()
            Log.d(TAG, "✅ Overlay callback executed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error calling overlay callback", e)
        }

        // إيقاف المراقبة بعد تأخير قصير
        handler.postDelayed({
            stopMonitoring()
        }, 200)
    }

    /**
     * فحص يدوي للـ Overlays النشطة
     */
    fun checkForActiveOverlays(): Boolean {
        return try {
            val hasFocus = activity.window.decorView.hasWindowFocus()

            if (!hasFocus) {
                Log.w(TAG, "App doesn't have window focus - possible overlay")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for overlays", e)
            false
        }
    }

    /**
     * استدعاء يدوي عند تغيير Focus (من ExamActivity)
     */
    fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!isMonitoring) return

        if (!hasFocus) {
            val now = System.currentTimeMillis()
            val timeSinceLastFocus = now - lastFocusTime
            val timeSinceLastLoss = now - lastFocusLossTime

            // ✅ تجاهل rapid changes
            if (timeSinceLastLoss < 300) {
                return
            }

            lastFocusLossTime = now

            if (timeSinceLastFocus > FOCUS_LOSS_THRESHOLD) {
                focusLossCount++
                Log.w(TAG, "📱 Manual focus check - Count: $focusLossCount/$FOCUS_COUNT_THRESHOLD")

                if (focusLossCount >= FOCUS_COUNT_THRESHOLD) {
                    // ✅ تأكيد إضافي قبل التسجيل
                    handler.postDelayed({
                        if (isMonitoring && !activity.hasWindowFocus()) {
                            handleOverlayDetected("MANUAL_FOCUS_LOST")
                        }
                    }, 500)
                }
            }
        } else {
            lastFocusTime = System.currentTimeMillis()
            handler.postDelayed({
                if (isMonitoring) {
                    focusLossCount = 0
                }
            }, 2000)
        }
    }
}