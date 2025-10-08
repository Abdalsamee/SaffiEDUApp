package com.example.saffieduapp.presentation.screens.student.exam_screen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.student.exam_screen.security.InExamScanUiState

@Composable
fun InExamRoomScanOverlay(
    state: InExamScanUiState
) {
    if (!state.visible) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF101427)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "مسح الغرفة بالكاميرا الخلفية",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "رجاءً لف الجهاز ببطء لمسح محيطك بالكامل. ارفع الهاتف قليلاً للأعلى ثم للأسفل.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(16.dp))

                // تقدّم الدوران الأفقي (Yaw)
                Text(text = "التغطية الأفقية", color = Color.White, fontSize = 14.sp)
                LinearProgressIndicator(
                    progress = state.coverage.yawCoveragePercent.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFF6C63FF)
                )
                Spacer(Modifier.height(8.dp))

                // تقدّم الإمالة الرأسي (Pitch)
                Text(text = "الإمالة (أعلى/أسفل)", color = Color.White, fontSize = 14.sp)
                LinearProgressIndicator(
                    progress = state.coverage.pitchCoveragePercent.coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFFF9800)
                )

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "الوقت المنقضي: ${(state.durationMs / 1000)} ثانية",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }

        // (اختياري) تلميحات أسهم/إرشادات إضافية في أسفل الشاشة
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1325)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "نصائح:",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "• لف الهاتف ببطء من اليسار إلى اليمين.\n• ارفع الهاتف قليلًا للأعلى، ثم اخفضه للأسفل.\n• استمر حتى يكتمل الشريطين.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }
        }
    }
}
