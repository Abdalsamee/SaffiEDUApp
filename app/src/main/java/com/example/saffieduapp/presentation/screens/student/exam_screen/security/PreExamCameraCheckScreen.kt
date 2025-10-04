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

    var faceCheckStatus by remember { mutableStateOf<FaceCheckStatus>(FaceCheckStatus.Checking) }
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initializeCamera()
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        )
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