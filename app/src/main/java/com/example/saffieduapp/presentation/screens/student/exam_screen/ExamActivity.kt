package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.*
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.*
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * ExamActivity - نشاط الاختبار الرئيسي
 * ✅ محدّث: دعم نظام مسح الغرفة والجلسات المحسّن
 */
@AndroidEntryPoint
class ExamActivity : ComponentActivity() {

    private lateinit var securityManager: ExamSecurityManager
    private lateinit var cameraViewModel: CameraMonitorViewModel
    private var examId: String = ""
    private var sessionId: String? = null // من RoomScanActivity

    private var showCameraCheck = mutableStateOf(true)
    private var cameraCheckPassed = mutableStateOf(false)

    // ✅ صلاحيات الكاميرا والصوت (للتسجيل)
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (!cameraGranted) {
            Toast.makeText(
                this,
                "صلاحية الكاميرا مطلوبة لبدء الاختبار",
                Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            // إذا كانت هذه أول مرة ولم يتم مسح الغرفة
            if (sessionId == null && !audioGranted) {
                // اطلب الصوت أيضاً لمسح الغرفة
                Toast.makeText(
                    this,
                    "صلاحية التسجيل مطلوبة لمسح الغرفة",
                    Toast.LENGTH_SHORT
                ).show()
            }
            initializeCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            examId = intent.getStringExtra("EXAM_ID") ?: ""
            sessionId = intent.getStringExtra("SESSION_ID") // من RoomScanActivity

            if (examId.isEmpty()) {
                Toast.makeText(this, "خطأ: معرف الاختبار مفقود", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            // فحص Multi-window
            if (isInMultiWindowMode) {
                Log.w("ExamActivity", "Multi-window detected at onCreate")
                showMultiWindowBlockedDialog()
                return
            }

            // تهيئة Security Manager
            securityManager = ExamSecurityManager(this, this)
            securityManager.enableSecurityFeatures()

            setupSecureScreen()

            // تهيئة ViewModel
            val studentId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            val factory = CameraMonitorViewModelFactory(
                application = application,
                onViolationDetected = { violationType ->
                    if (::securityManager.isInitialized) {
                        securityManager.logViolation(violationType)
                    }
                },
                examId = examId,
                studentId = studentId,
                existingSessionId = sessionId // ✅ تمرير الجلسة الموجودة
            )
            cameraViewModel = ViewModelProvider(this, factory)[CameraMonitorViewModel::class.java]

            // ربط CameraMonitor مع SecurityManager
            cameraViewModel.getCameraMonitor().let { monitor ->
                securityManager.setCameraMonitor(monitor)
            }

            // ✅ مراقبة حالة الجلسة
            cameraViewModel.getSessionState()?.let { sessionStateFlow ->
                lifecycleScope.launch {
                    sessionStateFlow.collect { session ->
                        session?.let {
                            Log.d("ExamActivity", """
                                📊 Session Update:
                                ID: ${it.sessionId}
                                Snapshots: ${it.snapshots.size}/${com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSession.MAX_SNAPSHOTS}
                                Violations: ${it.violations.size}
                                Events: ${it.securityEvents.size}
                                Status: ${it.status}
                                Has Video: ${it.backCameraVideo != null}
                            """.trimIndent())
                        }
                    }
                }
            }

            // ✅ مراقبة إحصائيات الصور
            cameraViewModel.getSnapshotStats()?.let { snapshotStatsFlow ->
                lifecycleScope.launch {
                    snapshotStatsFlow.collect { stats ->
                        Log.d("ExamActivity", """
                            📸 Snapshots Stats:
                            NoFace: ${stats.noFaceSnapshots}
                            Multiple: ${stats.multipleFacesSnapshots}
                            LookingAway: ${stats.lookingAwaySnapshots}
                            Manual: ${stats.manualSnapshots}
                            Periodic: ${stats.periodicSnapshots}
                            Total: ${stats.totalSuccessful}
                            Success Rate: ${String.format("%.1f", stats.successRate)}%
                            Failed: ${stats.failedAttempts}
                        """.trimIndent())
                    }
                }
            }

            // فحص وطلب الصلاحيات
            checkAndRequestCameraPermissions()

        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in onCreate", e)
            Toast.makeText(this, "خطأ في بدء الاختبار: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * ✅ فحص وطلب الصلاحيات المطلوبة
     */
    private fun checkAndRequestCameraPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        // إذا لم يتم مسح الغرفة بعد، نحتاج صلاحية التسجيل
        if (sessionId == null) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            initializeCamera()
        } else {
            cameraPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    /**
     * ✅ تهيئة الكاميرا وبدء الجلسة
     */
    private fun initializeCamera() {
        if (::cameraViewModel.isInitialized) {
            cameraViewModel.initializeCamera()

            // ✅ إذا كانت جلسة موجودة، حمّلها، وإلا ابدأ جديدة
            if (sessionId != null) {
                // الجلسة محمّلة بالفعل من ViewModel
                Log.d("ExamActivity", "✅ Using existing session: $sessionId")
            } else {
                // بدء جلسة جديدة
                cameraViewModel.startExamSession()
                Log.d("ExamActivity", "✅ Started new session")
            }
        }
        setupUI()
    }

    /**
     * إعداد واجهة المستخدم
     */
    private fun setupUI() {
        setContent {
            SaffiEDUAppTheme {
                val showCameraCheckScreen by showCameraCheck
                val checkPassed by cameraCheckPassed

                if (showCameraCheckScreen && !checkPassed && ::cameraViewModel.isInitialized) {
                    PreExamCameraCheckScreen(
                        viewModel = cameraViewModel,
                        onCheckPassed = {
                            cameraCheckPassed.value = true
                            showCameraCheck.value = false
                        },
                        onCheckFailed = { reason ->
                            Toast.makeText(
                                this@ExamActivity,
                                "فشل فحص الكاميرا: $reason",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    )
                } else if (checkPassed) {
                    ExamActivityContent()
                }
            }
        }
    }

    /**
     * محتوى الاختبار الرئيسي
     */
    @Composable
    private fun ExamActivityContent() {
        var showExitDialog by remember { mutableStateOf(false) }
        var showOverlayDialog by remember { mutableStateOf(false) }
        var overlayViolationType by remember { mutableStateOf("") }

        val showNoFaceWarning by securityManager.showNoFaceWarning.collectAsState()
        val showExitWarning by securityManager.showExitWarning.collectAsState()
        val showMultipleFacesWarning by securityManager.showMultipleFacesWarning.collectAsState()
        val shouldAutoSubmit by securityManager.shouldAutoSubmit.collectAsState()
        val isPaused by securityManager.isPaused.collectAsState()
        val violations by securityManager.violations.collectAsState()

        // مراقبة Face Detection
        if (::cameraViewModel.isInitialized) {
            val detectionResult by cameraViewModel.lastDetectionResult.collectAsState(initial = null)

            LaunchedEffect(detectionResult) {
                if (detectionResult is FaceDetectionResult.ValidFace) {
                    securityManager.resetMultipleFacesCount()
                }
            }
        }

        // منع زر الرجوع
        BackHandler {
            securityManager.logViolation("BACK_BUTTON_PRESSED")
            showExitDialog = true
        }

        // معالجة الإرسال التلقائي
        LaunchedEffect(shouldAutoSubmit) {
            if (shouldAutoSubmit) {
                val lastViolation = violations.lastOrNull()

                Log.d("ExamActivity", "Auto-submit triggered. Last violation: ${lastViolation?.type}, Severity: ${lastViolation?.severity}")

                when {
                    lastViolation?.severity == Severity.CRITICAL -> {
                        overlayViolationType = lastViolation.type
                        showOverlayDialog = true
                    }
                    else -> {
                        val message = when (lastViolation?.type) {
                            "OVERLAY_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف نافذة منبثقة"
                            "MULTI_WINDOW_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف وضع النوافذ المتعددة"
                            "EXTERNAL_DISPLAY_CONNECTED" -> "تم إنهاء الاختبار: تم اكتشاف شاشة خارجية"
                            "MULTIPLE_FACES_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف أكثر من شخص"
                            "NO_FACE_DETECTED_LONG" -> "تم إنهاء الاختبار: عدم ظهور الوجه لفترة طويلة"
                            else -> "تم إنهاء الاختبار تلقائياً"
                        }
                        Toast.makeText(this@ExamActivity, message, Toast.LENGTH_LONG).show()
                        finishExam()
                    }
                }
            }
        }

        // بدء المراقبة عند تحميل الشاشة
        LaunchedEffect(Unit) {
            securityManager.startMonitoring()
            securityManager.startExam()
        }

        // شاشة الاختبار
        ExamScreen(
            onNavigateUp = {
                securityManager.logViolation("NAVIGATE_UP_PRESSED")
                showExitDialog = true
            },
            onExamComplete = { finishExam() }
        )

        // Dialogs
        if (showExitDialog) {
            ExamExitWarningDialog(
                onDismiss = { showExitDialog = false },
                onConfirmExit = {
                    securityManager.logViolation("USER_FORCED_EXIT")
                    finishExam()
                }
            )
        }

        if (showNoFaceWarning) {
            NoFaceWarningDialog(
                violationCount = securityManager.getNoFaceViolationCount(),
                remainingWarnings = securityManager.getRemainingNoFaceWarnings(),
                isPaused = isPaused,
                onDismiss = {
                    securityManager.dismissNoFaceWarning()
                }
            )
        }

        if (showMultipleFacesWarning) {
            MultipleFacesWarningDialog(
                onDismiss = {
                    securityManager.dismissMultipleFacesWarning()
                }
            )
        }

        if (showExitWarning) {
            val exitCount = violations.count { it.type.startsWith("APP_RESUMED") }

            ExamReturnWarningDialog(
                exitAttempts = exitCount,
                remainingAttempts = securityManager.getRemainingAttempts(),
                onContinue = {
                    securityManager.dismissExitWarning()
                }
            )
        }

        if (showOverlayDialog) {
            OverlayDetectedDialog(
                violationType = overlayViolationType,
                onDismiss = {
                    showOverlayDialog = false
                    finishExam()
                }
            )
        }
    }

    /**
     * إعداد الشاشة الآمنة
     */
    private fun setupSecureScreen() {
        window.apply {
            setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
    }

    /**
     * عرض dialog لحظر Multi-window
     */
    private fun showMultiWindowBlockedDialog() {
        setContent {
            SaffiEDUAppTheme {
                MultiWindowBlockedDialog(
                    onDismiss = { finish() }
                )
            }
        }
    }

    // ============ Lifecycle Callbacks ============

    override fun onMultiWindowModeChanged(
        isInMultiWindowMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)

        if (isInMultiWindowMode) {
            Log.e("ExamActivity", "Multi-window mode activated during exam!")

            if (::securityManager.isInitialized) {
                securityManager.logViolation("MULTI_WINDOW_DETECTED")
            }

            Toast.makeText(
                this,
                "تم إنهاء الاختبار: تم اكتشاف وضع تقسيم الشاشة",
                Toast.LENGTH_LONG
            ).show()

            finishExam()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            Log.e("ExamActivity", "PIP mode detected!")

            if (::securityManager.isInitialized) {
                securityManager.logViolation("PIP_MODE_DETECTED")
            }

            Toast.makeText(
                this,
                "تم إنهاء الاختبار: لا يسمح بوضع Picture-in-Picture",
                Toast.LENGTH_LONG
            ).show()

            finishExam()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)

        if (isInMultiWindowMode && ::securityManager.isInitialized) {
            securityManager.logViolation("MULTI_WINDOW_CONFIG_CHANGE")
            finishExam()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::securityManager.isInitialized) {
            securityManager.onAppPaused()
        }

        if (::cameraViewModel.isInitialized) {
            cameraViewModel.pauseExamSession()
        }
    }

    override fun onResume() {
        super.onResume()

        if (isInMultiWindowMode) {
            if (::securityManager.isInitialized) {
                securityManager.logViolation("MULTI_WINDOW_ON_RESUME")
            }
            Toast.makeText(this, "تم اكتشاف وضع تقسيم الشاشة", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (::securityManager.isInitialized) {
            securityManager.onAppResumed()
        }

        if (::cameraViewModel.isInitialized) {
            cameraViewModel.resumeExamSession()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (::securityManager.isInitialized) {
            securityManager.onWindowFocusChanged(hasFocus)

            if (!hasFocus) {
                securityManager.logViolation("WINDOW_FOCUS_LOST")
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (::securityManager.isInitialized) {
            securityManager.logViolation("USER_LEFT_APP")
        }
    }

    /**
     * ✅ إنهاء الاختبار وطباعة التقرير الكامل
     */
    private fun finishExam() {
        try {
            if (::cameraViewModel.isInitialized) {
                cameraViewModel.endExamSession()

                // ✅ طباعة تقرير مفصل
                val stats = cameraViewModel.getSessionStats()
                stats?.let {
                    Log.d("ExamActivity", """
                        =====================================
                        📊 EXAM SESSION COMPLETED
                        =====================================
                        Session ID: ${it.sessionId}
                        Duration: ${it.duration / 1000}s (${it.duration / 60000}m ${(it.duration / 1000) % 60}s)
                        Snapshots: ${it.snapshotsCount}/${com.example.saffieduapp.presentation.screens.student.exam_screen.session.ExamSession.MAX_SNAPSHOTS}
                        Violations: ${it.violationsCount}
                        Security Events: ${it.eventsCount}
                        Back Camera Video: ${if (it.hasBackVideo) "✅ Recorded" else "❌ Not Recorded"}
                        Status: ${it.status}
                        =====================================
                    """.trimIndent())
                }

                // ✅ طباعة إحصائيات الصور
                val snapshotStats = cameraViewModel.getSnapshotStats()?.value
                snapshotStats?.let {
                    Log.d("ExamActivity", """
                        📸 SNAPSHOT STATISTICS
                        ────────────────────────────────────
                        No Face: ${it.noFaceSnapshots}
                        Multiple Faces: ${it.multipleFacesSnapshots}
                        Looking Away: ${it.lookingAwaySnapshots}
                        Manual: ${it.manualSnapshots}
                        Periodic: ${it.periodicSnapshots}
                        ────────────────────────────────────
                        Total Successful: ${it.totalSuccessful}
                        Total Attempts: ${it.totalAttempts}
                        Failed Attempts: ${it.failedAttempts}
                        Success Rate: ${String.format("%.2f", it.successRate)}%
                        ────────────────────────────────────
                    """.trimIndent())
                }
            }

            // ✅ تقرير الأمان
            if (::securityManager.isInitialized) {
                val report = securityManager.generateReport()
                Log.d("ExamActivity", """
                    🔐 SECURITY REPORT
                    $report
                """.trimIndent())
            }

            // تنظيف الموارد
            if (::cameraViewModel.isInitialized) {
                cameraViewModel.stopMonitoring()
            }

            if (::securityManager.isInitialized) {
                securityManager.cleanup()
            }

        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in finishExam", e)
        } finally {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            if (::securityManager.isInitialized) {
                securityManager.stopMonitoring()
                securityManager.cleanup()
            }

            if (::cameraViewModel.isInitialized) {
                cameraViewModel.cleanup()
            }
        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in onDestroy", e)
        }
    }
}