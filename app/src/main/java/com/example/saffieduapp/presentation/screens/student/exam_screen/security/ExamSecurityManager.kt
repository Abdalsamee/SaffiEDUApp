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
 * ExamSecurityManager
 * - كتم/تخفيف إيجابيات overlay الكاذبة أثناء الحوارات والعمليات الداخلية
 * - تحكم تدريجي في No-Face / Multiple-Faces مع تحذيرات قبل الإنهاء
 * - لا نعتمد مجرد وجود صلاحية SYSTEM_ALERT_WINDOW كدليل حاسم
 */
class ExamSecurityManager(
    private val context: Context,
    private val activity: Activity
) {

    private val TAG = "ExamSecurityManager"
    private val DIALOG_SUPPRESS_MS = 1500L

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

    // auto-submit channel
    private val _shouldAutoSubmit = MutableStateFlow(false)
    val shouldAutoSubmit: StateFlow<Boolean> = _shouldAutoSubmit.asStateFlow()

    // ==================== العدادات ====================
    private var appPausedTime: Long = 0
    private var totalTimeOutOfApp: Long = 0
    private var exitAttempts = 0
    private val maxExitAttempts = 2

    private var noFaceViolationCount = 0
    private val maxNoFaceWarnings = 2 // محاولتان قبل الإنهاء

    private var multipleFacesCount = 0
    private val maxMultipleFacesWarnings = 2 // محاولتان قبل الإنهاء

    private var examStarted = false

    private val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var overlayDetector: OverlayDetector? = null
    private var cameraMonitor: CameraMonitor? = null

    // ==================== الحماية الداخلية/العمليات المحمية ====================
    @Volatile private var internalDialogActive = false
    @Volatile private var overlayDetectionPaused = false

    // نستخدم set للأسماء + عداد للعمليات الداخلية لتفادي التكرارات والتصادمات
    private val activeInternalDialogs = mutableSetOf<String>()
    private var internalOpSeq = 0

    private val periodicCheckHandler = Handler(Looper.getMainLooper())
    private var periodicCheckRunnable: Runnable? = null

    companion object {
        const val DIALOG_EXIT_WARNING = "EXIT_WARNING"
        const val DIALOG_NO_FACE_WARNING = "NO_FACE_WARNING"
        const val DIALOG_MULTIPLE_FACES = "MULTIPLE_FACES"
        const val DIALOG_EXIT_RETURN = "EXIT_RETURN"
        const val DIALOG_OVERLAY_DETECTED = "OVERLAY_DETECTED"
        const val DIALOG_FINALIZE_FLOW = "FINALIZE_FLOW"
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
        overlayDetector = OverlayDetector(
            activity = activity,
            onOverlayDetected = {
                Log.e(TAG, "🚨 Overlay detected callback!")
                Handler(Looper.getMainLooper()).post {
                    if (!isInternalDialogActive()) {
                        // حالة حرجة فقط عندما لا توجد عملية داخلية جارية
                        logViolation("OVERLAY_DETECTED")
                        registerInternalDialog(DIALOG_OVERLAY_DETECTED)
                        _shouldShowWarning.value = true
                        pauseMonitoring()
                    } else {
                        // يتم تجاهلها إذا كنا في تدفق داخلي (مثال: Room Scan / Finalize)
                        Log.d(TAG, "Overlay ignored (internal dialog/operation active)")
                    }
                }
            },
            // مهم: كتم الكشف أثناء أي Dialog داخلي
            shouldSuppress = { isInternalDialogActive() }
        )
        startOverlayPeriodicCheck()
    }

    private fun startOverlayPeriodicCheck() {
        periodicCheckRunnable = object : Runnable {
            override fun run() {
                // لا نفحص قبل بدء الامتحان أو أثناء كتم المراقبة
                if (!examStarted || overlayDetectionPaused) {
                    periodicCheckHandler.postDelayed(this, 3000)
                    return
                }

                // لا نفحص أثناء وجود أي Dialog داخلي/عملية محمية
                if (isInternalDialogActive()) {
                    periodicCheckHandler.postDelayed(this, 3000)
                    return
                }

                // وجود صلاحية SYSTEM_OVERLAY: نسجل مخالفة LOW فقط - بدون ديالوج
                if (Settings.canDrawOverlays(context)) {
                    logViolation("OVERLAY_PERMISSION_ACTIVE") // LOW فقط
                }

                // ملاحظة: فقدان الفوكس يُدار بدقة داخل OverlayDetector نفسه.
                periodicCheckHandler.postDelayed(this, 3000)
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
    // 🧱 إدارة Dialogs والعمليات الداخلية
    // ==========================================================
    fun registerInternalDialog(dialogName: String) {
        synchronized(activeInternalDialogs) {
            activeInternalDialogs.add(dialogName)
            internalDialogActive = true
            // كتم مؤقت لتقلبات الفوكس عند ظهور/اختفاء عناصر الواجهة
            overlayDetector?.suppressFor(DIALOG_SUPPRESS_MS)
            pauseOverlayDetection()
        }
    }

    fun unregisterInternalDialog(dialogName: String) {
        synchronized(activeInternalDialogs) {
            activeInternalDialogs.remove(dialogName)
            if (activeInternalDialogs.isEmpty()) {
                internalDialogActive = false
                Handler(Looper.getMainLooper()).postDelayed({
                    overlayDetector?.suppressFor(400) // سماح بإنهاء أي أنيميشن
                    resumeOverlayDetection()
                }, 300)
            }
        }
    }

    /**
     * API عالي المستوى لكتم كشف الـ overlay أثناء عملية داخلية قصيرة.
     * يعيد "token" داخلي يتم التخلص منه تلقائيًا عند النداء الموازي لـ markInternalOperationEnd().
     *
     * ملاحظات:
     * - لست مضطرًا لإعادة تمرير نفس الوسم في end؛ أي استدعاء end سيُنهي عملية داخلية واحدة.
     * - لمن يريد الدقة: يمكن الاحتفاظ بالـ token وإرساله إلى end لإغلاق نفس العملية بعينها.
     */
    fun markInternalOperationStart(tag: String = "INTERNAL_OP"): String {
        val token = "INTERNAL_OP_${tag}_${++internalOpSeq}_${System.nanoTime()}"
        registerInternalDialog(token)
        return token
    }

    fun markInternalOperationEnd(tokenOrTag: String? = null) {
        synchronized(activeInternalDialogs) {
            // إذا زوّدنا token/Tag نحاول إزالة المطابق أولاً
            val direct = tokenOrTag?.let { tok ->
                activeInternalDialogs.firstOrNull { it == tok || it.contains(tok) }
            }
            if (direct != null) {
                unregisterInternalDialog(direct)
                return
            }
            // وإلا نزيل أي عملية داخلية متبقية
            val anyInternal = activeInternalDialogs.firstOrNull { it.startsWith("INTERNAL_OP_") }
            if (anyInternal != null) {
                unregisterInternalDialog(anyInternal)
            } else {
                // لا شيء لإزالته؛ نتأكد فقط من حالة الكتم
                if (activeInternalDialogs.isEmpty()) {
                    internalDialogActive = false
                    resumeOverlayDetection()
                }
            }
        }
    }

    // مساعدات لتغليف تدفق إنهاء/تسليم الامتحان (اختياري استعمالها من الـActivity)
    fun beginFinalizeFlow() = registerInternalDialog(DIALOG_FINALIZE_FLOW)
    fun endFinalizeFlow()   = unregisterInternalDialog(DIALOG_FINALIZE_FLOW)

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
            // لا نفتح تحذير — فقط تسجيل LOW
            if (Settings.canDrawOverlays(context)) {
                logViolation("OVERLAY_PERMISSION_ENABLED_AT_START") // LOW
                Log.w(TAG, "Overlay permission active before exam start (logged only)")
            }

            // لا نفتح تحذير عند فقدان الفوكس لحظة البدء — نترك OverlayDetector يتأكد
            val hasFocus = activity.hasWindowFocus()
            if (!hasFocus) {
                logViolation("FOCUS_LOST_AT_START") // LOW
                Log.w(TAG, "No window focus at start (logged only)")
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
        synchronized(activeInternalDialogs) { activeInternalDialogs.clear() }
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
            "NO_FACE_DETECTED_LONG"     -> handleNoFaceDetected()
            "MULTIPLE_FACES_DETECTED"   -> handleMultipleFacesDetected()
        }
    }

    private fun handleNoFaceDetected() {
        noFaceViolationCount++
        Log.w(TAG, "No face violation #$noFaceViolationCount")

        if (noFaceViolationCount > maxNoFaceWarnings) {
            // لا نفتح ديالوج Overlay — نفعّل auto-submit ونكتم الكشف مؤقتًا
            beginFinalizeFlow()
            _shouldAutoSubmit.value = true
            pauseMonitoring()
            Log.e(TAG, "🚨 Exceeded allowed no-face warnings — will auto-submit")
            return
        }

        if (!_showNoFaceWarning.value && !isInternalDialogActive()) {
            registerInternalDialog(DIALOG_NO_FACE_WARNING)
            _showNoFaceWarning.value = true
            pauseMonitoring()
            Log.w(TAG, "⚠️ No face warning displayed ($noFaceViolationCount/$maxNoFaceWarnings)")
        }
    }

    private fun handleMultipleFacesDetected() {
        multipleFacesCount++
        Log.w(TAG, "Multiple faces violation #$multipleFacesCount")

        if (multipleFacesCount > maxMultipleFacesWarnings) {
            beginFinalizeFlow()
            _shouldAutoSubmit.value = true
            pauseMonitoring()
            Log.e(TAG, "🚨 Multiple faces exceeded limit — will auto-submit")
            return
        }

        if (!_showMultipleFacesWarning.value && !isInternalDialogActive()) {
            registerInternalDialog(DIALOG_MULTIPLE_FACES)
            _showMultipleFacesWarning.value = true
            pauseMonitoring()
            Log.w(TAG, "⚠️ Multiple faces warning displayed ($multipleFacesCount/$maxMultipleFacesWarnings)")
        }
    }

    // ==========================================================
    // 🧠 وصف وشدة المخالفات
    // ==========================================================
    private fun getViolationDescription(type: String): String {
        return when {
            type == "OVERLAY_PERMISSION_ACTIVE" ||
                    type == "OVERLAY_PERMISSION_ENABLED_AT_START" -> "صلاحية الرسم فوق التطبيقات مفعّلة"

            type.startsWith("OVERLAY") -> "تم اكتشاف تطبيق يعمل فوق الاختبار"
            type.startsWith("NO_FACE") -> "لم يتم اكتشاف وجه الطالب"
            type.startsWith("MULTIPLE_FACES") -> "تم اكتشاف أكثر من وجه"
            type.startsWith("EXTERNAL_DISPLAY") -> "تم اكتشاف شاشة خارجية"
            type.contains("APP_RESUMED") -> "عودة من خارج التطبيق"
            type == "FOCUS_LOST_AT_START" -> "فقدان تركيز النافذة عند البدء"
            else -> "مخالفة أمنية عامة"
        }
    }

    private fun getViolationSeverity(type: String): Severity {
        return when {
            type == "OVERLAY_PERMISSION_ACTIVE" ||
                    type == "OVERLAY_PERMISSION_ENABLED_AT_START" ||
                    type == "FOCUS_LOST_AT_START" -> Severity.LOW

            type.startsWith("OVERLAY") -> Severity.CRITICAL
            type.startsWith("MULTIPLE_FACES") -> Severity.HIGH
            type.startsWith("NO_FACE") -> Severity.MEDIUM
            else -> Severity.LOW
        }
    }

    // ==========================================================
    // 🧩 دوال مطلوبة من ExamActivity
    // ==========================================================
    fun triggerAutoSubmit() { _shouldAutoSubmit.value = true }
    fun resetAutoSubmit() { _shouldAutoSubmit.value = false }

    fun resetMultipleFacesCount() {
        multipleFacesCount = 0
        Log.d(TAG, "✅ Multiple faces count reset")
    }

    fun getNoFaceViolationCount(): Int = noFaceViolationCount
    fun getRemainingNoFaceWarnings(): Int = (maxNoFaceWarnings - noFaceViolationCount).coerceAtLeast(0)
    fun getRemainingAttempts(): Int = (maxExitAttempts - exitAttempts).coerceAtLeast(0)

    fun onWindowFocusChanged(hasFocus: Boolean) {
        // نمرر حدث الفوكس للكاشف؛ وهو أصلاً سيحترم shouldSuppress()
        overlayDetector?.onWindowFocusChanged(hasFocus)
    }

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
