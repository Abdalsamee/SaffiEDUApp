package com.example.saffieduapp.presentation.screens.student.exam_screen.security

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

/**
 * شاشة فحص الكاميرا قبل بدء الاختبار - محسّنة
 * التحسينات: عرض تفصيلي للحالة + شريط تقدم + رسائل واضحة
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
    var validFaceDetectedCount by remember { mutableStateOf(0) }
    var initState by remember { mutableStateOf<InitState>(InitState.Idle) }
    var cameraAvailability by remember { mutableStateOf<CameraAvailability?>(null) }

    // التحسينات الجديدة
    var lastMessage by remember { mutableStateOf("في انتظار التحقق...") }
    var totalChecks by remember { mutableStateOf(0) }

    // تهيئة الكاميرا
    LaunchedEffect(Unit) {
        initState = InitState.Initializing
        try {
            viewModel.initializeCamera()
            delay(1500)

            val availability = viewModel.getCameraMonitor().checkCameraAvailability()
            cameraAvailability = availability

            if (availability.hasFrontCamera) {
                initState = InitState.Success
                showPreview = true
                Log.d("CameraCheck", "Camera initialized successfully")
            } else {
                initState = InitState.Error("الكاميرا الأمامية غير متوفرة")
                Log.e("CameraCheck", "Front camera not available")
            }
        } catch (e: Exception) {
            initState = InitState.Error(e.message ?: "خطأ في تهيئة الكاميرا")
            Log.e("CameraCheck", "Initialization failed", e)
        }
    }

    // إضافة عداد timestamp لتتبع التحديثات
    var lastUpdateTime by remember { mutableStateOf(0L) }

    // مراقبة نتائج الكشف مع timestamp
    LaunchedEffect(lastDetectionResult, lastUpdateTime) {
        if (lastDetectionResult == null) return@LaunchedEffect

        val result = lastDetectionResult ?: return@LaunchedEffect
        val currentTime = System.currentTimeMillis()

        // تحديث timestamp لإجبار LaunchedEffect على التشغيل
        if (result is FaceDetectionResult.ValidFace) {
            delay(100)
            lastUpdateTime = currentTime
        }

        lastDetectionResult?.let { result ->
            // تجنب العد المتكرر لنفس النتيجة
            if (result != lastDetectionResult) return@let

            totalChecks++
            Log.d("CameraCheck", "🔍 Check #$totalChecks - Result: $result")

            when (result) {
                is FaceDetectionResult.ValidFace -> {
                    validFaceDetectedCount++
                    lastMessage = "وجه صحيح ($validFaceDetectedCount/3)"
                    Log.d("CameraCheck", "Valid face $validFaceDetectedCount/3")

                    if (validFaceDetectedCount >= 3) {
                        faceCheckStatus = FaceCheckStatus.Passed
                        lastMessage = "تم التحقق بنجاح"
                        Log.d("CameraCheck", "CHECK PASSED - Proceeding to exam")
                        delay(800)
                        onCheckPassed()
                    } else {
                        faceCheckStatus = FaceCheckStatus.Checking
                    }
                }

                is FaceDetectionResult.NoFace -> {
                    validFaceDetectedCount = 0
                    lastMessage = "لم يتم اكتشاف وجه"
                    Log.w("CameraCheck", "No face detected - counter reset")
                    faceCheckStatus = FaceCheckStatus.Failed("لم يتم اكتشاف وجه - الرجاء التأكد من ظهور وجهك بوضوح")
                }

                is FaceDetectionResult.MultipleFaces -> {
                    validFaceDetectedCount = 0
                    lastMessage = "أكثر من وجه (${result.count})"
                    Log.w("CameraCheck", "Multiple faces: ${result.count} - counter reset")
                    faceCheckStatus = FaceCheckStatus.Failed("تم اكتشاف أكثر من وجه - يجب أن تكون وحيداً")
                }

                is FaceDetectionResult.LookingAway -> {
                    validFaceDetectedCount = 0
                    lastMessage = "انظر للكاميرا مباشرة"
                    Log.w("CameraCheck", "Looking away: ${result.angle}° - counter reset")
                    faceCheckStatus = FaceCheckStatus.Failed("الرجاء النظر مباشرة للكاميرا")
                }

                else -> {
                    lastMessage = "خطأ في الكشف"
                    Log.e("CameraCheck", "Detection error")
                }
            }
        }
    }

    // واجهة المستخدم
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "فحص الكاميرا",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "يرجى التأكد من أن وجهك مرئي بوضوح أمام الكاميرا",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.9f)
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

            // بطاقة التقدم - جديد
            ProgressCard(
                lastMessage = lastMessage,
                validCount = validFaceDetectedCount,
                totalChecks = totalChecks
            )

            Spacer(modifier = Modifier.height(16.dp))

            // حالة التهيئة
            when (val state = initState) {
                is InitState.Idle -> {
                    CheckStatusItem(
                        icon = Icons.Default.Warning,
                        text = "في انتظار التهيئة...",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                is InitState.Initializing -> {
                    CheckStatusItem(
                        icon = null,
                        text = "جارٍ تهيئة الكاميرا...",
                        color = MaterialTheme.colorScheme.primary,
                        showLoading = true
                    )
                }

                is InitState.Success -> {
                    CheckStatusItem(
                        icon = Icons.Default.CheckCircle,
                        text = "تم تهيئة الكاميرا بنجاح",
                        color = Color(0xFF4CAF50)
                    )
                }

                is InitState.Error -> {
                    CheckStatusItem(
                        icon = Icons.Default.Error,
                        text = "خطأ: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )

                    cameraAvailability?.let {
                        Text(
                            text = "Front=${it.hasFrontCamera}, Back=${it.hasBackCamera}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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

/**
 * بطاقة عرض التقدم - جديد
 */
@Composable
private fun ProgressCard(
    lastMessage: String,
    validCount: Int,
    totalChecks: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = lastMessage,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { validCount / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (validCount >= 3) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "إجمالي المحاولات: $totalChecks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CameraPreviewCard(viewModel: CameraMonitorViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var isMonitoringStarted by remember { mutableStateOf(false) }

    LaunchedEffect(previewView) {
        if (previewView != null && !isMonitoringStarted) {
            Log.d("CameraPreview", "Starting camera monitoring...")
            delay(500)

            previewView?.let { preview ->
                try {
                    viewModel.getCameraMonitor().startMonitoring(
                        lifecycleOwner = lifecycleOwner,
                        frontPreviewView = preview
                    )
                    isMonitoringStarted = true
                    Log.d("CameraPreview", "Camera monitoring started")
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Failed to start monitoring", e)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            Log.d("CameraPreview", "Preview disposed")
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
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
                        previewView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (!isMonitoringStarted) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = color,
                strokeWidth = 2.dp
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}

@Composable
private fun CameraAvailabilityCard(availability: CameraAvailability) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "الكاميرات المتاحة",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            CheckStatusItem(
                icon = if (availability.hasFrontCamera) Icons.Default.CheckCircle else Icons.Default.Error,
                text = "الكاميرا الأمامية: ${if (availability.hasFrontCamera) "متوفرة" else "غير متوفرة"}",
                color = if (availability.hasFrontCamera) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )

            CheckStatusItem(
                icon = if (availability.hasBackCamera) Icons.Default.CheckCircle else Icons.Default.Warning,
                text = "الكاميرا الخلفية: ${if (availability.hasBackCamera) "متوفرة" else "غير متوفرة"}",
                color = if (availability.hasBackCamera) Color(0xFF4CAF50) else MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun FaceCheckStatusCard(status: FaceCheckStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                is FaceCheckStatus.Passed -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                is FaceCheckStatus.Failed -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                is FaceCheckStatus.Checking -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "حالة كشف الوجه",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (status) {
                is FaceCheckStatus.Checking -> {
                    CheckStatusItem(
                        icon = null,
                        text = "جارٍ فحص الوجه...",
                        color = MaterialTheme.colorScheme.primary,
                        showLoading = true
                    )
                }

                is FaceCheckStatus.Passed -> {
                    CheckStatusItem(
                        icon = Icons.Default.CheckCircle,
                        text = "تم التعرف على الوجه بنجاح",
                        color = Color(0xFF4CAF50)
                    )
                }

                is FaceCheckStatus.Failed -> {
                    CheckStatusItem(
                        icon = Icons.Default.Error,
                        text = "فشل: ${status.reason}",
                        color = MaterialTheme.colorScheme.error
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
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text("إلغاء")
        }

        Button(
            onClick = onProceed,
            enabled = canProceed,
            modifier = Modifier.weight(1f)
        ) {
            Text("متابعة للاختبار")
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