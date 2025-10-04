package com.example.saffieduapp.presentation.screens.student.exam_screen.security

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay

/**
 * شاشة فحص الكاميرا قبل بدء الاختبار
 */
@Composable
fun PreExamCameraCheckScreen(
    viewModel: CameraMonitorViewModel,
    onCheckPassed: () -> Unit,
    onCheckFailed: (String) -> Unit
) {
    val initializationState by viewModel.initializationState.collectAsState()
    val cameraAvailability by viewModel.cameraAvailability.collectAsState()
    val monitoringState by viewModel.monitoringState.collectAsState()
    val lastDetectionResult by viewModel.lastDetectionResult.collectAsState()

    var faceCheckStatus by remember { mutableStateOf<FaceCheckStatus>(FaceCheckStatus.Checking) }
    var showPreview by remember { mutableStateOf(false) }
    var validFaceDetectedCount by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.initializeCamera()
    }

    // مراقبة نتائج Face Detection
    LaunchedEffect(lastDetectionResult) {
        lastDetectionResult?.let { result ->
            android.util.Log.d("CameraCheck", "Detection Result: $result")

            when (result) {
                is FaceDetectionResult.ValidFace -> {
                    validFaceDetectedCount++
                    android.util.Log.d("CameraCheck", "Valid face count: $validFaceDetectedCount")

                    // بعد 3 فحوصات ناجحة متتالية
                    if (validFaceDetectedCount >= 3) {
                        faceCheckStatus = FaceCheckStatus.Passed
                    } else {
                        faceCheckStatus = FaceCheckStatus.Checking
                    }
                }

                is FaceDetectionResult.NoFace -> {
                    validFaceDetectedCount = 0
                    faceCheckStatus = FaceCheckStatus.Failed("لم يتم اكتشاف وجه - الرجاء التأكد من ظهور وجهك بوضوح")
                }

                is FaceDetectionResult.MultipleFaces -> {
                    validFaceDetectedCount = 0
                    faceCheckStatus = FaceCheckStatus.Failed("تم اكتشاف أكثر من وجه - يجب أن تكون وحيداً")
                }

                is FaceDetectionResult.LookingAway -> {
                    validFaceDetectedCount = 0
                    faceCheckStatus = FaceCheckStatus.Failed("الرجاء النظر مباشرة للكاميرا")
                }

                is FaceDetectionResult.Error -> {
                    // لا تغير الحالة عند الخطأ
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // العنوان
            Text(
                text = "فحص الكاميرا",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "يرجى التأكد من أن وجهك مرئي بوضوح",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // معاينة الكاميرا
            if (showPreview) {
                CameraPreviewCard(viewModel)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // حالة التهيئة
            when (val state = initializationState) {
                is InitializationState.Idle -> {
                    CheckStatusItem(
                        icon = Icons.Default.Warning,
                        text = "في انتظار التهيئة...",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                is InitializationState.Initializing -> {
                    CheckStatusItem(
                        icon = null,
                        text = "جارٍ تهيئة الكاميرا...",
                        color = MaterialTheme.colorScheme.primary,
                        showLoading = true
                    )
                }

                is InitializationState.Success -> {
                    showPreview = true
                    CheckStatusItem(
                        icon = Icons.Default.CheckCircle,
                        text = "تم تهيئة الكاميرا بنجاح",
                        color = Color(0xFF4CAF50)
                    )
                }

                is InitializationState.Error -> {
                    CheckStatusItem(
                        icon = Icons.Default.Error,
                        text = "خطأ: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )

                    // عرض تفاصيل إضافية
                    Text(
                        text = "حالة الكاميرا: Front=${cameraAvailability?.hasFrontCamera}, Back=${cameraAvailability?.hasBackCamera}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // حالة توفر الكاميرات
            cameraAvailability?.let { availability ->
                CameraAvailabilityCard(availability)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // حالة كشف الوجه
            FaceCheckStatusCard(faceCheckStatus)

            Spacer(modifier = Modifier.weight(1f))

            // أزرار التحكم
            ControlButtons(
                canProceed = initializationState is InitializationState.Success &&
                        faceCheckStatus is FaceCheckStatus.Passed,
                onProceed = onCheckPassed,
                onCancel = { onCheckFailed("ألغى المستخدم الفحص") }
            )
        }
    }
}

@Composable
private fun CameraPreviewCard(viewModel: CameraMonitorViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isInitialized by viewModel.isInitialized.collectAsState()

    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    // بدء المعاينة فقط بعد التهيئة الناجحة
    LaunchedEffect(previewView, isInitialized) {
        if (isInitialized && previewView != null) {
            android.util.Log.d("CameraPreview", "Starting camera monitoring...")
            previewView?.let { preview ->
                try {
                    viewModel.getCameraMonitor().startMonitoring(
                        lifecycleOwner = lifecycleOwner,
                        frontPreviewView = preview
                    )
                    android.util.Log.d("CameraPreview", "✅ Camera monitoring started")
                } catch (e: Exception) {
                    android.util.Log.e("CameraPreview", "❌ Failed to start monitoring", e)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("CameraPreview", "Stopping preview monitoring")
            // إيقاف المراقبة - ستبدأ مرة أخرى في الخلفية
            viewModel.stopMonitoring()
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

            // مؤشر التحميل إذا لم تبدأ المعاينة بعد
            if (!isInitialized) {
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
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
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

/**
 * حالة فحص الوجه
 */
sealed class FaceCheckStatus {
    object Checking : FaceCheckStatus()
    object Passed : FaceCheckStatus()
    data class Failed(val reason: String) : FaceCheckStatus()
}