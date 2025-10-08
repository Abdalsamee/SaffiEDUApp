package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ✅ ExamSecurityManager (النسخة النهائية المتوافقة مع ExamActivity)
 * - كشف تطبيقات Overlay والمراقبة الدورية
 * - تحذيرات تدريجية لعدم وجود وجه أو تعدد الوجوه
 * - إنهاء الاختبار فقط في الحالات الحرجة
 * - تكامل كامل مع ExamActivity والـ Dialogs
 */
class ExamSecurityManager(
    private val context: Context,
    private val activity: Activity
) {
    private val TAG = "ExamSecurityManager"

    // ==================== الحالة العامة ====================
    private val _violations = MutableStateFlow<List<SecurityViolation>>(emptyList())
    val violations: StateFlow<List<SecurityViolation>> = _violations.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _shouldShowWarning = MutableStateFlow(false)
    val shouldShowWarning: StateFlow<Boolean> = _shouldShowWarning.asStateFlow()

    private val _showExitWarning = MutableStateFlow(false)
    val showExitWarning: StateFlow<Boolean> = _showExitWarning.asStateFlow()

    private val _showNoFaceWarning = MutableStateFlow(false)
    val showNoFaceWarning: StateFlow<Boolean> = _showNoFaceWarning.asStateFlow()

    private val _showMultipleFacesWarning = MutableStateFlow(false)
    val showMultipleFacesWarning: StateFlow<Boolean> = _showMultipleFacesWarning.asStateFlow()

    // ==================== العدادات ====================
    private var appPausedTime: Long = 0
    private var totalTimeOutOfApp: Long = 0
    private var exitAttempts = 0
    private val maxExitAttempts = 2

    private var noFaceViolationCount = 0
    private val maxNoFaceWarnings = 2
    private val maxNoFaceBeforeStrongWarning = 5

    private var multipleFacesCount = 0
    private val maxMultipleFacesWarnings = 2

    private var examStarted = false

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var overlayDetector: OverlayDetector? = null
    private var cameraMonitor: CameraMonitor? = null

    // ==================== الحماية الداخلية ====================
    @Volatile private var internalDialogActive = false
    @Volatile private var overlayDetectionPaused = false

    private val activeInternalDialogs = mutableSetOf<String>()
    private val periodicCheckHandler = Handler(Looper.getMainLooper())
    private var periodicCheckRunnable: Runnable? = null

    companion object {
        const val DIALOG_EXIT_WARNING = "EXIT_WARNING"
        const val DIALOG_NO_FACE_WARNING = "NO_FACE_WARNING"
        const val DIALOG_MULTIPLE_FACES = "MULTIPLE_FACES"
        const val DIALOG_EXIT_RETURN = "EXIT_RETURN"
        const val DIALOG_OVERLAY_DETECTED = "OVERLAY_DETECTED"
    }

    // ==========================================================
    // 🔒 تفعيل النظام الأمني
    // ==========================================================
    fun enableSecurityFeatures() {
        try {
            setupExternalDisplayMonitoring()
            setupOverlayDetection()
            Log.d(TAG, "✅ Security features enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling security features", e)
        }
    }

    // ==========================================================
    // 🧠 كشف الـ Overlay
    // ==========================================================
    private fun setupOverlayDetection() {
        overlayDetector = OverlayDetector(activity) {
            Log.e(TAG, "🚨 Overlay detected callback!")
            Handler(Looper.getMainLooper()).post {
                if (!isInternalDialogActive()) {
                    logViolation("OVERLAY_DETECTED")
                    registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                    _shouldShowWarning.value = true
                    pauseMonitoring()
                }
            }
        }
        startOverlayPeriodicCheck()
    }

    private fun startOverlayPeriodicCheck() {
        periodicCheckRunnable = object : Runnable {
            override fun run() {
                if (examStarted) {
                    if (Settings.canDrawOverlays(context) && !isInternalDialogActive()) {
                        logViolation("OVERLAY_PERMISSION_ACTIVE")
                        registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                        _shouldShowWarning.value = true
                        pauseMonitoring()
                        Log.e(TAG, "🚨 Overlay app detected via system permission")
                        return
                    }

                    if (!activity.hasWindowFocus() && !isInternalDialogActive()) {
                        logViolation("OVERLAY_FOCUS_LOST")
                        registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                        _shouldShowWarning.value = true
                        pauseMonitoring()
                        Log.w(TAG, "⚠️ Lost focus - possible overlay")
                        return
                    }

                    periodicCheckHandler.postDelayed(this, 3000)
                }
            }
        }
        periodicCheckHandler.postDelayed(periodicCheckRunnable!!, 3000)
    }

    private fun stopOverlayPeriodicCheck() {
        periodicCheckRunnable?.let {
            periodicCheckHandler.removeCallbacks(it)
            periodicCheckRunnable = null
        }
    }

    // ==========================================================
    // 🧱 إدارة Dialogs
    // ==========================================================
    fun registerInternalDialog(dialogName: String) {
        synchronized(activeInternalDialogs) {
            activeInternalDialogs.add(dialogName)
            internalDialogActive = true
            pauseOverlayDetection()
        }
    }

    fun unregisterInternalDialog(dialogName: String) {
        synchronized(activeInternalDialogs) {
            activeInternalDialogs.remove(dialogName)
            if (activeInternalDialogs.isEmpty()) {
                internalDialogActive = false
                resumeOverlayDetection()
            }
        }
    }

    private fun isInternalDialogActive(): Boolean {
        return synchronized(activeInternalDialogs) {
            internalDialogActive || activeInternalDialogs.isNotEmpty()
        }
    }

    private fun pauseOverlayDetection() {
        if (overlayDetectionPaused) return
        overlayDetectionPaused = true
        overlayDetector?.stopMonitoring()
    }

    private fun resumeOverlayDetection() {
        if (!overlayDetectionPaused) return
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isInternalDialogActive()) {
                overlayDetectionPaused = false
                overlayDetector?.startMonitoring()
            }
        }, 500)
    }

    // ==========================================================
    // 🖥️ مراقبة الشاشات الخارجية
    // ==========================================================
    private fun setupExternalDisplayMonitoring() {
        displayManager.registerDisplayListener(
            object : DisplayManager.DisplayListener {
                override fun onDisplayAdded(displayId: Int) {
                    if (displayId != 0) {
                        logViolation("EXTERNAL_DISPLAY_CONNECTED")
                        registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                        _shouldShowWarning.value = true
                        pauseMonitoring()
                        Log.e(TAG, "🚨 External display detected!")
                    }
                }
                override fun onDisplayRemoved(displayId: Int) {}
                override fun onDisplayChanged(displayId: Int) {}
            },
            null
        )
    }

    // ==========================================================
    // 📷 مراقبة الكاميرا
    // ==========================================================
    fun setCameraMonitor(monitor: CameraMonitor) {
        this.cameraMonitor = monitor
        Log.d(TAG, "Camera monitor linked")
    }

    private fun checkForExistingOverlays(): Boolean {
        return try {
            if (Settings.canDrawOverlays(context)) {
                logViolation("OVERLAY_PERMISSION_ENABLED_AT_START")
                registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                _shouldShowWarning.value = true
                pauseMonitoring()
                Log.e(TAG, "🚨 Overlay permission active before exam start!")
                return true
            }

            val hasFocus = activity.hasWindowFocus()
            if (!hasFocus) {
                logViolation("OVERLAY_DETECTED_AT_START")
                registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                _shouldShowWarning.value = true
                pauseMonitoring()
                Log.w(TAG, "⚠️ No window focus detected at start!")
                return true
            }

            false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for existing overlays", e)
            false
        }
    }

    // ==========================================================
    // 🧩 التحكم في المراقبة
    // ==========================================================
    fun startMonitoring() {
        checkForExistingOverlays()
        overlayDetector?.startMonitoring()
        Log.d(TAG, "Monitoring started")
    }

    fun startExam() { examStarted = true }

    fun pauseMonitoring() {
        _isPaused.value = true
        cameraMonitor?.pauseMonitoring()
    }

    fun resumeMonitoring() {
        _isPaused.value = false
        cameraMonitor?.resumeMonitoring()
        if (!overlayDetectionPaused && periodicCheckRunnable == null) {
            startOverlayPeriodicCheck()
        }
        Log.d(TAG, "✅ Monitoring resumed")
    }

    fun stopMonitoring() {
        stopOverlayPeriodicCheck()
        overlayDetector?.stopMonitoring()
        overlayDetector = null
        cameraMonitor?.cleanup()
        cameraMonitor = null
    }

    fun cleanup() {
        stopMonitoring()
        activeInternalDialogs.clear()
        internalDialogActive = false
        overlayDetectionPaused = false
    }

    // ==========================================================
    // ⚙️ دورة حياة التطبيق
    // ==========================================================
    fun onAppPaused() {
        if (!examStarted) return
        appPausedTime = System.currentTimeMillis()
        pauseMonitoring()
    }

    fun onAppResumed() {
        if (!examStarted) return
        if (appPausedTime > 0) {
            totalTimeOutOfApp += System.currentTimeMillis() - appPausedTime
            exitAttempts++
            logViolation("APP_RESUMED_AFTER_EXIT_$exitAttempts")

            if (exitAttempts > maxExitAttempts) {
                logViolation("MAX_EXIT_ATTEMPTS_REACHED")
                registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                _shouldShowWarning.value = true
                pauseMonitoring()
                Log.e(TAG, "🚨 Exam terminated — too many exits")
            } else {
                registerInternalDialog(DIALOG_EXIT_RETURN)
                _showExitWarning.value = true
                resumeMonitoring()
            }
            appPausedTime = 0
        }
    }

    // ==========================================================
    // 🚨 إدارة التحذيرات
    // ==========================================================
    fun dismissWarning() {
        unregisterInternalDialog(DIALOG_OVERLAY_DETECTED)
        _shouldShowWarning.value = false
        resumeMonitoring()
    }

    fun dismissExitWarning() {
        unregisterInternalDialog(DIALOG_EXIT_RETURN)
        _showExitWarning.value = false
        resumeMonitoring()
    }

    fun dismissNoFaceWarning() {
        unregisterInternalDialog(DIALOG_NO_FACE_WARNING)
        _showNoFaceWarning.value = false
        resumeMonitoring()
    }

    fun dismissMultipleFacesWarning() {
        unregisterInternalDialog(DIALOG_MULTIPLE_FACES)
        _showMultipleFacesWarning.value = false
        resumeMonitoring()
    }

    // ==========================================================
    // 🧾 تسجيل المخالفات
    // ==========================================================
    fun logViolation(type: String) {
        val violation = SecurityViolation(
            type = type,
            timestamp = System.currentTimeMillis(),
            description = getViolationDescription(type),
            severity = getViolationSeverity(type)
        )
        _violations.value = _violations.value + violation

        when (type) {
            "NO_FACE_DETECTED_LONG" -> handleNoFaceDetected()
            "MULTIPLE_FACES_DETECTED" -> handleMultipleFacesDetected()
        }
    }

    private fun handleNoFaceDetected() {
        noFaceViolationCount++
        Log.w(TAG, "No face violation #$noFaceViolationCount")

        // عدد التحذيرات المسموح بها (محاولتان)
        val maxWarningsAllowed = 2

        // إذا تجاوز الحد المسموح به، يتم إنهاء الاختبار
        if (noFaceViolationCount > maxWarningsAllowed) {
            logViolation("NO_FACE_FINAL_WARNING")
            registerInternalDialog(DIALOG_OVERLAY_DETECTED)
            _shouldShowWarning.value = true
            pauseMonitoring()
            Log.e(TAG, "🚨 Exceeded allowed no-face warnings — exam will end")
            return
        }

        // إذا لم يتجاوز الحد، يعرض تحذير فقط
        if (!_showNoFaceWarning.value && !isInternalDialogActive()) {
            registerInternalDialog(DIALOG_NO_FACE_WARNING)
            _showNoFaceWarning.value = true
            pauseMonitoring()
            Log.w(TAG, "⚠️ No face warning displayed ($noFaceViolationCount/$maxWarningsAllowed)")
        }
    }


    private fun handleMultipleFacesDetected() {
        multipleFacesCount++
        val maxWarningsAllowed = 2

        if (multipleFacesCount > maxWarningsAllowed) {
            logViolation("MULTIPLE_FACES_FINAL_WARNING")
            registerInternalDialog(DIALOG_OVERLAY_DETECTED)
            _shouldShowWarning.value = true
            pauseMonitoring()
            Log.e(TAG, "🚨 Multiple faces exceeded limit — exam will end")
            return
        }

        if (!_showMultipleFacesWarning.value && !isInternalDialogActive()) {
            registerInternalDialog(DIALOG_MULTIPLE_FACES)
            _showMultipleFacesWarning.value = true
            pauseMonitoring()
            Log.w(TAG, "⚠️ Multiple faces warning displayed ($multipleFacesCount/$maxWarningsAllowed)")
        }
    }


    // ==========================================================
    // 🧠 وصف وشدة المخالفات
    // ==========================================================
    private fun getViolationDescription(type: String): String {
        return when {
            type.startsWith("OVERLAY") -> "تم اكتشاف تطبيق يعمل فوق الاختبار"
            type.startsWith("NO_FACE") -> "لم يتم اكتشاف وجه الطالب"
            type.startsWith("MULTIPLE_FACES") -> "تم اكتشاف أكثر من وجه"
            type.startsWith("EXTERNAL_DISPLAY") -> "تم اكتشاف شاشة خارجية"
            type.contains("APP_RESUMED") -> "عودة من خارج التطبيق"
            else -> "مخالفة أمنية عامة"
        }
    }

    private fun getViolationSeverity(type: String): Severity {
        return when {
            type.startsWith("OVERLAY") -> Severity.CRITICAL
            type.startsWith("MULTIPLE_FACES") -> Severity.HIGH
            type.startsWith("NO_FACE") -> Severity.MEDIUM
            else -> Severity.LOW
        }
    }

    // ==========================================================
    // 🧩 دوال مطلوبة من ExamActivity
    // ==========================================================
    private val _shouldAutoSubmit = MutableStateFlow(false)
    val shouldAutoSubmit: StateFlow<Boolean> = _shouldAutoSubmit.asStateFlow()

    fun triggerAutoSubmit() { _shouldAutoSubmit.value = true }
    fun resetAutoSubmit() { _shouldAutoSubmit.value = false }

    // إعادة عدّاد الوجوه المتعددة (يستدعيها الـ UI عند رؤية وجه صالح)
    fun resetMultipleFacesCount() {
        multipleFacesCount = 0
        Log.d(TAG, "✅ Multiple faces count reset")
    }

    // عدّ مخالفات عدم وجود وجه
    fun getNoFaceViolationCount(): Int = noFaceViolationCount

    // التحذيرات المتبقية لعدم وجود وجه
    fun getRemainingNoFaceWarnings(): Int = (maxNoFaceWarnings - noFaceViolationCount).coerceAtLeast(0)

    // محاولات الخروج المتبقية
    fun getRemainingAttempts(): Int = (maxExitAttempts - exitAttempts).coerceAtLeast(0)

    // تمرير تغيّر الفوكس من الـ Activity إلى كاشف الـ overlay
    fun onWindowFocusChanged(hasFocus: Boolean) {
        overlayDetector?.onWindowFocusChanged(hasFocus)
    }

    // إنشاء تقرير نهائي للمخالفات
    fun generateReport(): SecurityReport {
        return SecurityReport(
            violations = _violations.value,
            totalExitAttempts = exitAttempts,
            totalTimeOutOfApp = totalTimeOutOfApp,
            noFaceViolations = noFaceViolationCount
        )
    }

}

// ==========================================================
// 🧩 الكيانات المساعدة
// ==========================================================
enum class Severity { LOW, MEDIUM, HIGH, CRITICAL }

data class SecurityViolation(
    val type: String,
    val timestamp: Long,
    val description: String,
    val severity: Severity = Severity.LOW
)

data class SecurityReport(
    val violations: List<SecurityViolation>,
    val totalExitAttempts: Int,
    val totalTimeOutOfApp: Long,
    val noFaceViolations: Int
)
