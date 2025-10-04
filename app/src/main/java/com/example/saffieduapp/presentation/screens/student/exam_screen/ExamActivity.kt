package com.example.saffieduapp.presentation.screens.student.exam_screen

import android.Manifest
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
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.ExamReturnWarningDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.ExamExitWarningDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.OverlayDetectedDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.MultiWindowBlockedDialog
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.*
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity منفصلة للاختبار مع حماية كاملة
 */
@AndroidEntryPoint
class ExamActivity : ComponentActivity() {

    private lateinit var securityManager: ExamSecurityManager
    private lateinit var cameraViewModel: CameraMonitorViewModel
    private var examId: String = ""

    private var showCameraCheck = mutableStateOf(true)
    private var cameraCheckPassed = mutableStateOf(false)

    // طلب صلاحيات الكاميرا
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

        if (!cameraGranted) {
            Toast.makeText(
                this,
                "صلاحية الكاميرا مطلوبة لبدء الاختبار",
                Toast.LENGTH_LONG
            ).show()
            finish()
        } else {
            // تهيئة الكاميرا
            initializeCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // الحصول على examId
            examId = intent.getStringExtra("EXAM_ID") ?: ""

            // ✅ فحص Multi-Window قبل بدء الاختبار
            if (isInMultiWindowMode) {
                Log.w("ExamActivity", "Multi-window detected at onCreate")
                showMultiWindowBlockedDialog()
                return
            }

            // تفعيل الحماية الأمنية
            securityManager = ExamSecurityManager(this, this)
            securityManager.enableSecurityFeatures()

            // إعداد الشاشة
            setupSecureScreen()

            // إنشاء Camera ViewModel
            val factory = CameraMonitorViewModelFactory(
                application = application,
                onViolationDetected = { violationType ->
                    // تسجيل المخالفة في Security Manager
                    if (::securityManager.isInitialized) {
                        securityManager.logViolation(violationType)
                    }
                }
            )
            cameraViewModel = ViewModelProvider(this, factory)[CameraMonitorViewModel::class.java]

            // ربط CameraMonitor مع SecurityManager
            cameraViewModel.getCameraMonitor().let { monitor ->
                securityManager.setCameraMonitor(monitor)
            }

            // طلب صلاحيات الكاميرا
            checkAndRequestCameraPermissions()

        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in onCreate", e)
            Toast.makeText(this, "خطأ في بدء الاختبار", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    /**
     * فحص وطلب صلاحيات الكاميرا
     */
    private fun checkAndRequestCameraPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA
        )

        val allGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            initializeCamera()
        } else {
            cameraPermissionLauncher.launch(permissions)
        }
    }

    /**
     * تهيئة الكاميرا
     */
    private fun initializeCamera() {
        if (::cameraViewModel.isInitialized) {
            cameraViewModel.initializeCamera()
        }
        setupUI()
    }

    /**
     * إعداد الواجهة
     */
    private fun setupUI() {
        setContent {
            SaffiEDUAppTheme {
                val showCameraCheckScreen by showCameraCheck
                val checkPassed by cameraCheckPassed

                // إذا لم يتم الفحص بعد، اعرض شاشة الفحص
                if (showCameraCheckScreen && !checkPassed && ::cameraViewModel.isInitialized) {
                    PreExamCameraCheckScreen(
                        viewModel = cameraViewModel,
                        onCheckPassed = {
                            cameraCheckPassed.value = true
                            showCameraCheck.value = false

                            // ✅ بدء المراقبة في الخلفية (بدون preview)
                            startBackgroundMonitoring()
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
                    // الاختبار الفعلي
                    ExamActivityContent()
                }
            }
        }
    }

    /**
     * بدء المراقبة في الخلفية (بدون عرض)
     */
    private fun startBackgroundMonitoring() {
        try {
            cameraViewModel.getCameraMonitor().startMonitoring(
                lifecycleOwner = this,
                frontPreviewView = null // ✅ بدون preview - مراقبة خفية
            )
            Log.d("ExamActivity", "✅ Background monitoring started")
        } catch (e: Exception) {
            Log.e("ExamActivity", "❌ Failed to start background monitoring", e)
        }
    }

    /**
     * محتوى الاختبار الفعلي
     */
    @Composable
    private fun ExamActivityContent() {
        var showExitDialog by remember { mutableStateOf(false) }
        var showOverlayDialog by remember { mutableStateOf(false) }
        var overlayViolationType by remember { mutableStateOf("") }

        val shouldShowWarning by securityManager.shouldShowWarning.collectAsState()
        val shouldAutoSubmit by securityManager.shouldAutoSubmit.collectAsState()
        val isPaused by securityManager.isPaused.collectAsState()
        val violations by securityManager.violations.collectAsState()

        // اعتراض زر الرجوع
        BackHandler {
            securityManager.logViolation("BACK_BUTTON_PRESSED")
            showExitDialog = true
        }

        // إنهاء تلقائي عند الوصول للحد الأقصى
        LaunchedEffect(shouldAutoSubmit) {
            if (shouldAutoSubmit) {
                val lastViolation = violations.lastOrNull()

                if (lastViolation?.severity == Severity.CRITICAL) {
                    overlayViolationType = lastViolation.type
                    showOverlayDialog = true
                } else {
                    val message = when (lastViolation?.type) {
                        "OVERLAY_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف نافذة منبثقة"
                        "MULTI_WINDOW_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف وضع النوافذ المتعددة"
                        "EXTERNAL_DISPLAY_CONNECTED" -> "تم إنهاء الاختبار: تم اكتشاف شاشة خارجية"
                        "MULTIPLE_FACES_DETECTED" -> "تم إنهاء الاختبار: تم اكتشاف أكثر من شخص"
                        "NO_FACE_DETECTED_LONG" -> "تم إنهاء الاختبار: عدم ظهور الوجه لفترة طويلة"
                        else -> "تم إنهاء الاختبار تلقائياً بسبب تجاوز محاولات الخروج"
                    }

                    Toast.makeText(this@ExamActivity, message, Toast.LENGTH_LONG).show()
                    finishExam()
                }
            }
        }

        // مراقبة Lifecycle
        LaunchedEffect(Unit) {
            securityManager.startMonitoring()
        }

        // شاشة الاختبار الأصلية (بدون cameraViewModel)
        ExamScreen(
            onNavigateUp = {
                securityManager.logViolation("NAVIGATE_UP_PRESSED")
                showExitDialog = true
            },
            onExamComplete = {
                finishExam()
            }
        )

        // Dialog تحذير الخروج
        if (showExitDialog) {
            ExamExitWarningDialog(
                onDismiss = { showExitDialog = false },
                onConfirmExit = {
                    securityManager.logViolation("USER_FORCED_EXIT")
                    finishExam()
                }
            )
        }

        // Dialog تحذير العودة
        if (shouldShowWarning) {
            val exitCount = remember(shouldShowWarning) {
                securityManager.violations.value.count {
                    it.type.startsWith("APP_RESUMED")
                }
            }

            ExamReturnWarningDialog(
                exitAttempts = exitCount,
                remainingAttempts = securityManager.getRemainingAttempts(),
                onContinue = {
                    securityManager.dismissWarning()
                }
            )
        }

        // Dialog تحذير Overlay
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

    private fun showMultiWindowBlockedDialog() {
        setContent {
            SaffiEDUAppTheme {
                MultiWindowBlockedDialog(
                    onDismiss = { finish() }
                )
            }
        }
    }

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
                "⚠️ تم إنهاء الاختبار: تم اكتشاف وضع تقسيم الشاشة",
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
    }

    override fun onResume() {
        super.onResume()

        // فحص Multi-Window عند العودة
        if (isInMultiWindowMode) {
            if (::securityManager.isInitialized) {
                securityManager.logViolation("MULTI_WINDOW_ON_RESUME")
            }
            Toast.makeText(this, "⚠️ تم اكتشاف وضع تقسيم الشاشة", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (::securityManager.isInitialized) {
            securityManager.onAppResumed()
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

    private fun finishExam() {
        try {
            if (::securityManager.isInitialized) {
                val report = securityManager.generateReport()
                Log.d("ExamActivity", "Security Report: $report")
                // TODO: رفع التقرير للسيرفر
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
        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in onDestroy", e)
        }
    }
}