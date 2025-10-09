package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

// ألوان التطبيق
private val AppPrimary = Color(0xFF4A90E2)
private val CardBackgroundColor = Color(0xFFD2E3F8)
private val AppBackground = Color(0xFFFFFFFF)
private val AppAlert = Color(0xFFF2994A)
private val AppAccent = Color(0xFF6FCF97)
private val AppTextPrimary = Color(0xFF333333)
private val AppTextSecondary = Color(0xFF828282)

/**
 * شاشة فحص الكاميرا قبل بدء الاختبار - نسخة محسّنة مع تصميم متناسق
 */
@Composable
fun PreExamCameraCheckScreen(
    viewModel: CameraMonitorViewModel,
    onCheckPassed: () -> Unit,
    onCheckFailed: (String) -> Unit
) {
    val lastDetectionResult by viewModel.lastDetectionResult.collectAsState(initial = null)

    var faceCheckStatus by remember { mutableStateOf<FaceCheckStatus>(FaceCheckStatus.Checking) }
    var showPreview by remember { mutableStateOf(false) }
    var validFaceDetectedStreak by remember { mutableStateOf(0) }
    var initState by remember { mutableStateOf<InitState>(InitState.Idle) }
    var cameraAvailability by remember { mutableStateOf<CameraAvailability?>(null) }

    var lastMessage by remember { mutableStateOf("في انتظار التحقق...") }

    // تهيئة الكاميرا
    LaunchedEffect(Unit) {
        initState = InitState.Initializing
        try {
            viewModel.initializeCamera()
            delay(1000)

            val availability = viewModel.getCameraMonitor().checkCameraAvailability()
            cameraAvailability = availability

            if (availability.hasFrontCamera) {
                initState = InitState.Success
                showPreview = true
                Log.d("CameraCheck", "✅ Camera initialized successfully")
            } else {
                initState = InitState.Error("الكاميرا الأمامية غير متوفرة")
                Log.e("CameraCheck", "❌ Front camera not available")
            }
        } catch (e: Exception) {
            initState = InitState.Error(e.message ?: "خطأ في تهيئة الكاميرا")
            Log.e("CameraCheck", "❌ Initialization failed", e)
        }
    }

    // مراقبة نتائج الكشف
    LaunchedEffect(lastDetectionResult) {
        val result = lastDetectionResult ?: return@LaunchedEffect

        when (result) {
            is FaceDetectionResult.ValidFace -> {
                validFaceDetectedStreak++
                lastMessage = "تم اكتشاف وجهك بوضوح"
                Log.d("CameraCheck", "✅ Valid face (streak=$validFaceDetectedStreak)")

                if (validFaceDetectedStreak >= 3) {
                    faceCheckStatus = FaceCheckStatus.Passed
                    lastMessage = "تم التحقق بنجاح ✔"
                    delay(500)
                    onCheckPassed()
                } else {
                    faceCheckStatus = FaceCheckStatus.Checking
                }
            }

            is FaceDetectionResult.NoFace -> {
                validFaceDetectedStreak = 0
                lastMessage = "لم يتم اكتشاف وجه"
                Log.w("CameraCheck", "⚠️ No face detected")
                faceCheckStatus = FaceCheckStatus.Failed("لم يتم اكتشاف وجه - الرجاء التأكد من ظهور وجهك بوضوح")
            }

            is FaceDetectionResult.MultipleFaces -> {
                validFaceDetectedStreak = 0
                lastMessage = if (result.faceCount > 1) {
                    "تم اكتشاف أكثر من وجه (${result.faceCount})"
                } else {
                    "تم اكتشاف أكثر من وجه"
                }
                Log.w("CameraCheck", "⚠️ Multiple faces: ${result.faceCount}")
                faceCheckStatus = FaceCheckStatus.Failed("تم اكتشاف أكثر من وجه - يجب أن تكون وحيداً أمام الكاميرا")
            }

            is FaceDetectionResult.LookingAway -> {
                validFaceDetectedStreak = 0
                lastMessage = "الرجاء النظر مباشرة للكاميرا"
                Log.w("CameraCheck", "⚠️ Looking away: Y=${result.eulerY}° Z=${result.eulerZ}°")
                faceCheckStatus = FaceCheckStatus.Failed("الرجاء النظر مباشرة للكاميرا")
            }
        }
    }

    // واجهة المستخدم المحسّنة
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // العنوان الرئيسي
            Text(
                text = "فحص الكاميرا",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = AppPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // الوصف
            Text(
                text = "تأكد أن وجهك مرئي بوضوح أمام الكاميرا قبل بدء الاختبار.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = AppTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.92f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // معاينة الكاميرا
            if (showPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                ) {
                    CameraPreviewCard(viewModel)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // رسالة الحالة
            AssistChipLikeMessage(lastMessage, faceCheckStatus)

            Spacer(modifier = Modifier.height(20.dp))

            // حالة التهيئة
            when (val state = initState) {
                is InitState.Idle -> {
                    CheckStatusItem(
                        icon = Icons.Default.Warning,
                        text = "في انتظار التهيئة...",
                        color = AppAlert
                    )
                }

                is InitState.Initializing -> {
                    CheckStatusItem(
                        icon = null,
                        text = "جارٍ تهيئة الكاميرا...",
                        color = AppPrimary,
                        showLoading = true
                    )
                }

                is InitState.Success -> {
                    CheckStatusItem(
                        icon = Icons.Default.CheckCircle,
                        text = "تم تهيئة الكاميرا بنجاح",
                        color = AppAccent
                    )
                }

                is InitState.Error -> {
                    CheckStatusItem(
                        icon = Icons.Default.Error,
                        text = "خطأ: ${state.message}",
                        color = Color(0xFFEB5757)
                    )

                    cameraAvailability?.let {
                        Text(
                            text = "Front=${it.hasFrontCamera}, Back=${it.hasBackCamera}",
                            fontSize = 12.sp,
                            color = AppTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            cameraAvailability?.let {
                CameraAvailabilityCard(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            FaceCheckStatusCard(faceCheckStatus)

            Spacer(modifier = Modifier.height(32.dp))

            ControlButtons(
                canProceed = initState is InitState.Success && faceCheckStatus is FaceCheckStatus.Passed,
                onProceed = onCheckPassed,
                onCancel = { onCheckFailed("ألغى المستخدم الفحص") }
            )
        }
    }
}

/** شارة رسالة محسّنة */
@Composable
private fun AssistChipLikeMessage(message: String, status: FaceCheckStatus) {
    val backgroundColor = when (status) {
        is FaceCheckStatus.Passed -> AppAccent.copy(alpha = 0.15f)
        is FaceCheckStatus.Failed -> Color(0xFFEB5757).copy(alpha = 0.12f)
        is FaceCheckStatus.Checking -> CardBackgroundColor
    }

    val textColor = when (status) {
        is FaceCheckStatus.Passed -> AppAccent
        is FaceCheckStatus.Failed -> Color(0xFFEB5757)
        is FaceCheckStatus.Checking -> AppPrimary
    }

    Surface(
        color = backgroundColor,
        contentColor = textColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
        )
    }
}

@Composable
private fun CameraPreviewCard(viewModel: CameraMonitorViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var isMonitoringStarted by remember { mutableStateOf(false) }

    LaunchedEffect(previewViewRef) {
        if (previewViewRef != null && !isMonitoringStarted) {
            Log.d("CameraPreview", "▶️ Starting camera monitoring...")
            delay(400)

            previewViewRef?.let { preview ->
                try {
                    viewModel.getCameraMonitor().startMonitoring(
                        lifecycleOwner = lifecycleOwner,
                        frontPreviewView = preview
                    )
                    isMonitoringStarted = true
                    Log.d("CameraPreview", "✅ Camera monitoring started")
                } catch (e: Exception) {
                    Log.e("CameraPreview", "❌ Failed to start monitoring", e)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { Log.d("CameraPreview", "🔴 Preview disposed") }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        previewViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (!isMonitoringStarted) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppPrimary,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun CheckStatusItem(
    icon: ImageVector?,
    text: String,
    color: Color,
    showLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = color,
                strokeWidth = 2.5.dp
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun CameraAvailabilityCard(availability: CameraAvailability) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "الكاميرات المتاحة",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            CheckStatusItem(
                icon = if (availability.hasFrontCamera) Icons.Default.CheckCircle else Icons.Default.Error,
                text = "الكاميرا الأمامية: ${if (availability.hasFrontCamera) "متوفرة" else "غير متوفرة"}",
                color = if (availability.hasFrontCamera) AppAccent else Color(0xFFEB5757)
            )

            CheckStatusItem(
                icon = if (availability.hasBackCamera) Icons.Default.CheckCircle else Icons.Default.Warning,
                text = "الكاميرا الخلفية: ${if (availability.hasBackCamera) "متوفرة" else "غير متوفرة"}",
                color = if (availability.hasBackCamera) AppAccent else AppAlert
            )
        }
    }
}

@Composable
private fun FaceCheckStatusCard(status: FaceCheckStatus) {
    val (backgroundColor, borderColor) = when (status) {
        is FaceCheckStatus.Passed -> AppAccent.copy(alpha = 0.12f) to AppAccent
        is FaceCheckStatus.Failed -> Color(0xFFEB5757).copy(alpha = 0.1f) to Color(0xFFEB5757)
        is FaceCheckStatus.Checking -> CardBackgroundColor to AppPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "حالة كشف الوجه",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (status) {
                is FaceCheckStatus.Checking -> {
                    CheckStatusItem(
                        icon = null,
                        text = "جارٍ فحص الوجه...",
                        color = AppPrimary,
                        showLoading = true
                    )
                }

                is FaceCheckStatus.Passed -> {
                    CheckStatusItem(
                        icon = Icons.Default.CheckCircle,
                        text = "تم التعرف على الوجه بنجاح",
                        color = AppAccent
                    )
                }

                is FaceCheckStatus.Failed -> {
                    CheckStatusItem(
                        icon = Icons.Default.Error,
                        text = "فشل: ${status.reason}",
                        color = Color(0xFFEB5757)
                    )
                }
            }
        }
    }
}

@Composable
private fun ControlButtons(
    canProceed: Boolean,
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = AppTextPrimary
            )
        ) {
            Text(
                text = "إلغاء",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Button(
            onClick = onProceed,
            enabled = canProceed,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppPrimary,
                contentColor = Color.White,
                disabledContainerColor = AppTextSecondary.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = "متابعة للاختبار",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

sealed class InitState {
    object Idle : InitState()
    object Initializing : InitState()
    object Success : InitState()
    data class Error(val message: String) : InitState()
}

sealed class FaceCheckStatus {
    object Checking : FaceCheckStatus()
    object Passed : FaceCheckStatus()
    data class Failed(val reason: String) : FaceCheckStatus()
}