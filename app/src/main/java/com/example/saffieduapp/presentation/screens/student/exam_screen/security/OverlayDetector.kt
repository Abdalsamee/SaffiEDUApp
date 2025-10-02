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
 *
 * يعمل عن طريق:
 * 1. مراقبة Focus Changes عبر ViewTreeObserver
 * 2. وضع View شفاف فوق الشاشة وفحص إذا كان Touch يصل له
 * 3. مراقبة Window Visibility Changes
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

    // للكشف عن Overlays عبر Touch Events
    private var detectorView: View? = null
    private var windowManager: WindowManager? = null

    private var visibilityCheckRunnable: Runnable? = null
    private var focusChangeListener: ViewTreeObserver.OnWindowFocusChangeListener? = null

    /**
     * بدء المراقبة
     */
    fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        lastFocusTime = System.currentTimeMillis()

        // 1. مراقبة Window Focus
        setupWindowFocusMonitoring()

        // 2. إنشاء Detector View (لـ Android 6+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setupOverlayDetectorView()
        }

        Log.d(TAG, "Overlay monitoring started")
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
        Log.d(TAG, "Overlay monitoring stopped")
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

                // إذا فقدنا Focus لأكثر من 500ms معناه في Overlay حقيقي
                // (وليس مجرد Dialog داخلي)
                if (timeSinceLastFocus > 500) {
                    focusLossCount++
                    Log.w(TAG, "Focus lost! Count: $focusLossCount, Duration: ${timeSinceLastFocus}ms")

                    // بعد فقدان Focus مرتين نعتبرها مخالفة
                    if (focusLossCount >= 2) {
                        handleOverlayDetected("WINDOW_FOCUS_LOST")
                    }
                }
            } else {
                lastFocusTime = System.currentTimeMillis()
                // Reset counter عند استعادة Focus
                handler.postDelayed({
                    if (isMonitoring) {
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
     * إذا كان في Overlay فوقنا، الـ Touch Events مش هتوصل للـ View
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupOverlayDetectorView() {
        try {
            windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

            // إنشاء View شفاف بحجم 1x1 في الزاوية
            detectorView = View(activity).apply {
                setBackgroundColor(0x00000000) // شفاف تماماً
            }

            val params = WindowManager.LayoutParams(
                1, // width
                1, // height
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            // إضافة View للـ Window
            windowManager?.addView(detectorView, params)

            // مراقبة Touch Events
            detectorView?.setOnTouchListener { _, _ ->
                // إذا وصل Touch Event معناه مافيش Overlay يمنعه
                Log.d(TAG, "Touch detected - No overlay blocking")
                false
            }

            // فحص دوري إذا الـ View لسه Visible
            startPeriodicVisibilityCheck()

        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup detector view", e)
        }
    }

    /**
     * فحص دوري لـ Visibility
     */
    private fun startPeriodicVisibilityCheck() {
        visibilityCheckRunnable = object : Runnable {
            override fun run() {
                if (!isMonitoring) return

                detectorView?.let { view ->
                    try {
                        // فحص إذا الـ View مخفي أو مش Attached
                        if (!view.isAttachedToWindow) {
                            handleOverlayDetected("DETECTOR_VIEW_DETACHED")
                            return
                        }

                        if (view.visibility != View.VISIBLE) {
                            handleOverlayDetected("DETECTOR_VIEW_HIDDEN")
                            return
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error checking visibility", e)
                    }
                }

                // إعادة الفحص كل 2 ثانية
                if (isMonitoring) {
                    handler.postDelayed(this, 2000)
                }
            }
        }

        handler.postDelayed(visibilityCheckRunnable!!, 2000)
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

        Log.e(TAG, "⚠️ OVERLAY DETECTED: $reason")

        // استدعاء الـ Callback
        handler.post {
            try {
                onOverlayDetected()
            } catch (e: Exception) {
                Log.e(TAG, "Error calling overlay callback", e)
            }
        }

        // إيقاف المراقبة لتجنب Multiple Triggers
        stopMonitoring()
    }

    /**
     * فحص يدوي إذا في Overlays نشطة
     */
    fun checkForActiveOverlays(): Boolean {
        return try {
            // فحص إذا التطبيق عنده Focus
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

            if (timeSinceLastFocus > 500) {
                focusLossCount++
                Log.w(TAG, "Manual focus check - Count: $focusLossCount")

                if (focusLossCount >= 2) {
                    handleOverlayDetected("MANUAL_FOCUS_LOST")
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