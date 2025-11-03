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
import com.example.saffieduapp.data.FireBase.WorkManager.ExamUploadWorker
import com.example.saffieduapp.presentation.screens.student.exam_screen.components.*
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.*
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExamActivity : ComponentActivity() {

    var isInternalDialogVisible = false

    // الأمن والكاميرا
    private lateinit var securityManager: ExamSecurityManager
    private lateinit var cameraViewModel: CameraMonitorViewModel
    private lateinit var coverageTracker: RoomScanCoverageTracker
    private var randomScheduler: RandomEventScheduler? = null

    private var examId: String = ""
    private var sessionId: String? = null

    private var showCameraCheck = mutableStateOf(true)
    private var cameraCheckPassed = mutableStateOf(false)

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false

        if (!cameraGranted) {
            Toast.makeText(this, "صلاحية الكاميرا مطلوبة لبدء الاختبار", Toast.LENGTH_LONG).show()
            finish()
        } else {
            if (sessionId == null && !audioGranted) {
                Toast.makeText(this, "صلاحية التسجيل مطلوبة لمسح الغرفة", Toast.LENGTH_SHORT).show()
            }
            initializeCamera()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            examId = intent.getStringExtra("EXAM_ID") ?: ""
            sessionId = intent.getStringExtra("SESSION_ID")

            if (examId.isEmpty()) {
                Toast.makeText(this, "خطأ: معرف الاختبار مفقود", Toast.LENGTH_SHORT).show()
                finish(); return
            }

            if (isInMultiWindowMode) {
                Log.w("ExamActivity", "Multi-window detected at onCreate")
                showMultiWindowBlockedDialog()
                return
            }

            securityManager = ExamSecurityManager(this, this)
            securityManager.enableSecurityFeatures()
            setupSecureScreen()

            val studentId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (studentId.isEmpty()) {
                Toast.makeText(this, "لم يتم العثور على معرف الطالب", Toast.LENGTH_LONG).show()
                finish(); return
            }

            // ViewModel الكاميرا
            val factory = CameraMonitorViewModelFactory(
                application = application,
                onViolationDetected = { violationType ->
                    if (::securityManager.isInitialized) {
                        securityManager.logViolation(violationType)
                    }
                },
                examId = examId,
                studentId = studentId,
                existingSessionId = sessionId
            )
            cameraViewModel = ViewModelProvider(this, factory)[CameraMonitorViewModel::class.java]

            // ربط المراقب الأمني بمراقب الكاميرا
            cameraViewModel.getCameraMonitor().let { monitor ->
                securityManager.setCameraMonitor(monitor)
            }

            // متعقب تغطية المسح (حساسات)
            coverageTracker = RoomScanCoverageTracker(this)

            // طلب الصلاحيات
            checkAndRequestCameraPermissions()

        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in onCreate", e)
            Toast.makeText(this, "خطأ في بدء الاختبار: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun checkAndRequestCameraPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (sessionId == null) permissions.add(Manifest.permission.RECORD_AUDIO)

        val allGranted = permissions.all { p ->
            ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) initializeCamera()
        else cameraPermissionLauncher.launch(permissions.toTypedArray())
    }

    private fun initializeCamera() {
        if (::cameraViewModel.isInitialized) {
            val token = securityManager.markInternalOperationStart("InitializeCamera")
            try {
                cameraViewModel.initializeCamera()
                if (sessionId != null) {
                    Log.d("ExamActivity", "✅ Using existing session: $sessionId")
                } else {
                    cameraViewModel.startExamSession()
                    Log.d("ExamActivity", "✅ Started new session")
                }
            } finally {
                securityManager.markInternalOperationEnd(token)
            }
        }
        setupUI()
    }


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
                            Toast.makeText(this@ExamActivity, "فشل فحص الكاميرا: $reason", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    )
                } else if (checkPassed) {
                    ExamActivityContent()
                }
            }
        }
    }
    @Composable
    private fun SuppressOverlayWhileShown(tag: String, content: @Composable () -> Unit) {
        DisposableEffect(tag) {
            // أعلِم الـ Activity أننا نعرض Dialog داخلي
            isInternalDialogVisible = true

            // كتم منظومة الأمان (OverlayDetector) عبر قناع داخلي آمن
            val token = securityManager.markInternalOperationStart(tag)

            onDispose {
                // إعادة الحالة
                isInternalDialogVisible = false
                securityManager.markInternalOperationEnd(token)
            }
        }
        content()
    }



    @Composable
    private fun ExamActivityContent() {
        var showExitDialog by remember { mutableStateOf(false) }

        val showNoFaceWarning by securityManager.showNoFaceWarning.collectAsState()
        val showExitWarning by securityManager.showExitWarning.collectAsState()
        val showMultipleFacesWarning by securityManager.showMultipleFacesWarning.collectAsState()
        val showOverlayWarning by securityManager.shouldShowWarning.collectAsState()
        val autoSubmit by securityManager.shouldAutoSubmit.collectAsState()

        val isPaused by securityManager.isPaused.collectAsState()
        val violations by securityManager.violations.collectAsState()

        // واجهة المسح داخل الامتحان
        var showRoomScanOverlay by remember { mutableStateOf(false) }
        val coverage by coverageTracker.state.collectAsState()
        var currentScanElapsedMs by remember { mutableStateOf(0L) }

        // تحديث زمن التسجيل في واجهة المسح
        LaunchedEffect(showRoomScanOverlay) {
            if (showRoomScanOverlay) {
                while (true) {
                    randomScheduler?.let { currentScanElapsedMs = it.getCurrentRecordingMs() }
                    kotlinx.coroutines.delay(300)
                }
            }
        }

        if (::cameraViewModel.isInitialized) {
            val detectionResult by cameraViewModel.lastDetectionResult.collectAsState(initial = null)
            LaunchedEffect(detectionResult) {
                if (detectionResult is FaceDetectionResult.ValidFace) {
                    securityManager.resetMultipleFacesCount()
                }
            }
        }


        LaunchedEffect(autoSubmit) {
            if (autoSubmit) {
                val token = securityManager.markInternalOperationStart("AutoSubmitExam")
                try {
                    Toast.makeText(this@ExamActivity, "تم إنهاء الاختبار تلقائيًا.", Toast.LENGTH_SHORT).show()
                    finishExam()
                } finally {
                    securityManager.markInternalOperationEnd(token)
                    securityManager.resetAutoSubmit()
                }
            }
        }


        // منع الرجوع
        BackHandler {
            if (!showExitDialog && !showRoomScanOverlay) {
                securityManager.logViolation("BACK_BUTTON_PRESSED")
                securityManager.registerInternalDialog(ExamSecurityManager.DIALOG_EXIT_WARNING)
                showExitDialog = true
            }
        }

        // بدء المراقبة + جدولة الأحداث العشوائية
        LaunchedEffect(Unit) {
            securityManager.startMonitoring()
            securityManager.startExam()

            val cm = cameraViewModel.getCameraMonitor()
            val sessionMgr = cm.getSessionManager()
            val backRecorder = BackCameraVideoRecorder(this@ExamActivity, sessionMgr)

            randomScheduler = RandomEventScheduler(
                frontSnapshotManager = cm.getFrontSnapshotManager(),
                backCameraRecorder = backRecorder,
                sessionManager = sessionMgr,
                lifecycleOwner = this@ExamActivity,
                pauseFrontDetection = { cm.pauseMonitoring() },
                resumeFrontDetection = { cm.resumeMonitoring() },
                onShowRoomScanOverlay = {
                    val token = securityManager.markInternalOperationStart("RoomScanStart")
                    coverageTracker.reset()
                    coverageTracker.start()
                    showRoomScanOverlay = true
                    securityManager.registerInternalDialog("ROOM_SCAN")
                    securityManager.markInternalOperationEnd(token)
                },
                onHideRoomScanOverlay = {
                    val token = securityManager.markInternalOperationStart("RoomScanEnd")
                    showRoomScanOverlay = false
                    coverageTracker.stop()
                    securityManager.unregisterInternalDialog("ROOM_SCAN")
                    securityManager.markInternalOperationEnd(token)
                },
                coverageTracker = coverageTracker
            ).also { it.startRandomEvents() }
        }

        // شاشة الامتحان
        ExamScreen(
            onNavigateUp = {
                if (!showExitDialog && !showRoomScanOverlay) {
                    securityManager.logViolation("NAVIGATE_UP_PRESSED")
                    securityManager.registerInternalDialog(ExamSecurityManager.DIALOG_EXIT_WARNING)
                    showExitDialog = true
                }
            },
            onExamComplete = { finishExam() },
            examId = examId // متغير الذي حصلت عليه من Intent
                  ,  onFinalDialogOpen = {
                securityManager.registerInternalDialog("DIALOG_FINAL_SUBMIT")
            },
            onFinalDialogClose = {
                securityManager.unregisterInternalDialog("DIALOG_FINAL_SUBMIT")
            }
        )

        // Overlay للمسح أثناء الامتحان
        if (showRoomScanOverlay) {
            InExamRoomScanOverlay(
                state = InExamScanUiState(
                    visible = true,
                    durationMs = currentScanElapsedMs,
                    coverage = coverage
                )
            )
        }

        // الحوارات
        if (showExitDialog) {
            SuppressOverlayWhileShown(ExamSecurityManager.DIALOG_EXIT_WARNING) {
                ExamExitWarningDialog(
                    onDismiss = {
                        showExitDialog = false
                        securityManager.unregisterInternalDialog(ExamSecurityManager.DIALOG_EXIT_WARNING)
                    },
                    onConfirmExit = {
                        securityManager.logViolation("USER_FORCED_EXIT")
                        securityManager.unregisterInternalDialog(ExamSecurityManager.DIALOG_EXIT_WARNING)
                        finishExam()
                    }
                )
            }
        }


        if (showNoFaceWarning) {
            SuppressOverlayWhileShown(ExamSecurityManager.DIALOG_NO_FACE_WARNING) {
                NoFaceWarningDialog(
                    violationCount = securityManager.getNoFaceViolationCount(),
                    remainingWarnings = securityManager.getRemainingNoFaceWarnings(),
                    isPaused = isPaused,
                    onDismiss = { securityManager.dismissNoFaceWarning() }
                )
            }
        }


        if (showMultipleFacesWarning) {
            SuppressOverlayWhileShown(ExamSecurityManager.DIALOG_MULTIPLE_FACES) {
                MultipleFacesWarningDialog(
                    onDismiss = { securityManager.dismissMultipleFacesWarning() }
                )
            }
        }


        if (showExitWarning) {
            val exitCount = violations.count { it.type.startsWith("APP_RESUMED") }
            SuppressOverlayWhileShown(ExamSecurityManager.DIALOG_EXIT_RETURN) {
                ExamReturnWarningDialog(
                    exitAttempts = exitCount,
                    remainingAttempts = securityManager.getRemainingAttempts(),
                    onContinue = { securityManager.dismissExitWarning() }
                )
            }
        }


        // تحذير الـ Overlay (حالات حرجة)
        if (showOverlayWarning) {
            OverlayDetectedDialog(
                violationType = "OVERLAY_DETECTED",
                onDismiss = {
                    securityManager.dismissWarning()
                    finishExam()
                }
            )
        }
    }

    private fun setupSecureScreen() {
        window.apply {
            setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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
            SaffiEDUAppTheme { MultiWindowBlockedDialog(onDismiss = { finish() }) }
        }
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        if (isInMultiWindowMode) {
            Log.e("ExamActivity", "Multi-window mode activated during exam!")
            if (::securityManager.isInitialized) securityManager.logViolation("MULTI_WINDOW_DETECTED")
            Toast.makeText(this, "تم إنهاء الاختبار: تم اكتشاف وضع تقسيم الشاشة", Toast.LENGTH_LONG).show()
            finishExam()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            Log.e("ExamActivity", "PIP mode detected!")
            if (::securityManager.isInitialized) securityManager.logViolation("PIP_MODE_DETECTED")
            Toast.makeText(this, "تم إنهاء الاختبار: لا يسمح بوضع Picture-in-Picture", Toast.LENGTH_LONG).show()
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
        if (::securityManager.isInitialized) securityManager.onAppPaused()
        if (::cameraViewModel.isInitialized) cameraViewModel.pauseExamSession()
    }

    override fun onResume() {
        super.onResume()
        if (isInMultiWindowMode) {
            if (::securityManager.isInitialized) securityManager.logViolation("MULTI_WINDOW_ON_RESUME")
            Toast.makeText(this, "تم اكتشاف وضع تقسيم الشاشة", Toast.LENGTH_LONG).show()
            finish(); return
        }
        if (::securityManager.isInitialized) securityManager.onAppResumed()
        if (::cameraViewModel.isInitialized) cameraViewModel.resumeExamSession()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (::securityManager.isInitialized) securityManager.onWindowFocusChanged(hasFocus)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (::securityManager.isInitialized) securityManager.logViolation("USER_LEFT_APP")
    }

    private fun finishExam() {
        try {
            val token = securityManager.markInternalOperationStart("FinalizeExam")

            randomScheduler?.stop()
            randomScheduler = null

            if (::cameraViewModel.isInitialized) cameraViewModel.endExamSession()

            if (::securityManager.isInitialized) {
                val report = securityManager.generateReport()
                Log.d("ExamActivity", """
                🔐 SECURITY REPORT
                Total Violations: ${report.violations.size}
                Exit Attempts: ${report.totalExitAttempts}
                Time Out: ${report.totalTimeOutOfApp}ms
            """.trimIndent())
            }

            if (::cameraViewModel.isInitialized) cameraViewModel.stopMonitoring()
            if (::securityManager.isInitialized) securityManager.cleanup()

            securityManager.markInternalOperationEnd(token)

        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in finishExam", e)
        } finally {
            try {
                if (::cameraViewModel.isInitialized) {
                    val sessionManager = cameraViewModel.getSessionManager()
                    val session = sessionManager.getCurrentSession()

                    if (session != null) {
                        val studentId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
                        val mediaFiles = sessionManager.getLocalMediaFiles()
                        val sessionJson = sessionManager.exportSessionForUpload()

                        // تجهيز البيانات لـ WorkManager
                        val mediaPaths = mediaFiles.map { it.absolutePath }
                        val inputData = androidx.work.Data.Builder()
                            .putString("examId", session.examId)
                            .putString("studentId", studentId)
                            .putString("sessionId", session.sessionId)
                            .putString("sessionJson", sessionJson)
                            .putString("mediaPaths", com.google.gson.Gson().toJson(mediaPaths))
                            .build()

                        // إنشاء مهمة WorkManager
                        val uploadRequest = androidx.work.OneTimeWorkRequestBuilder<ExamUploadWorker>()
                            .setInputData(inputData)
                            .setConstraints(
                                androidx.work.Constraints.Builder()
                                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                                    .build()
                            )
                            .build()

                        // جدولة الرفع في الخلفية
                        androidx.work.WorkManager.getInstance(this)
                            .enqueue(uploadRequest)

                        Log.d("ExamUploadWorker", "📤 Upload task scheduled in background")
                    }
                }
            } catch (uploadError: Exception) {
                Log.e("ExamUploadWorker", "❌ Failed to schedule upload", uploadError)
            }

            // ← هذا السطر يبقى في النهاية كما هو
            finish()
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            randomScheduler?.stop()
            randomScheduler = null
            if (::securityManager.isInitialized) {
                securityManager.stopMonitoring()
                securityManager.cleanup()
            }
            if (::cameraViewModel.isInitialized) cameraViewModel.cleanup()
        } catch (e: Exception) {
            Log.e("ExamActivity", "Error in onDestroy", e)
        }
    }
}
