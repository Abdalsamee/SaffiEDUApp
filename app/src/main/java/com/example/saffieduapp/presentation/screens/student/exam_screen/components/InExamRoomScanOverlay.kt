package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.InExamScanUiState

@Composable
fun InExamRoomScanOverlay(state: InExamScanUiState) {
    val progress = (state.elapsedMs.toFloat() / state.targetMs).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "مسح محيطك بالكاميرا الخلفية",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "من فضلك حرّك الهاتف ببطء لليمين واليسار، ثم ارفع الهاتف قليلاً وأنزله لمسح كامل الغرفة.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black.copy(alpha = 0.75f)
                )

                // مؤشّر عام للمدة
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                // مؤشرات توجيهية بسيطة
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabeledProgress("الدوران الأفقي", state.yawProgress)
                    LabeledProgress("الإمالة للأعلى/الأسفل", state.pinchProgressCompat())
                    LabeledProgress("التدوير", state.rollProgress)
                }

                Text(
                    text = if (state.allDirectionsCovered) "تمت تغطية كل الاتجاهات تقريباً" else "تابع حتى يكتمل الشريط",
                    fontSize = 13.sp,
                    color = if (state.allDirectionsCovered) Color(0xFF2E7D32) else Color(0xFFB71C1C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/** اسم دالة مساعد فقط لأن الاسم الصحيح pitchProgress لكن نعرضها كما هي */
@Composable
private fun LabeledProgress(label: String, value: Float) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(
            progress = value.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        )
    }
}

private fun InExamScanUiState.pinchProgressCompat(): Float = this.pitchProgress
