package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherStudentExamScreen(
    navController: NavController? = null
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "نظام المراقبة",
                onNavigateUp = { navController?.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {

            // 🔹 الصف العلوي: الطالب يمين، الإجراءات يسار
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                ExamStudentInfoCard(
                    name = "يزن عادل ظهير",
                    imageUrl = "https://randomuser.me/api/portraits/men/60.jpg"
                )

                ExamActionColumn()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 تفاصيل الامتحان
            ExamDetailsSection()

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 محاولات الغش
            CheatingLogsSection(
                logs = listOf(
                    "10:05 ص → خرج من التطبيق (تنبيه)",
                    "10:15 ص → أوقف الكاميرا",
                    "10:20 ص → عودة للامتحان"
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 لقطات الصور والفيديو
            ExamMediaSection(
                imageUrls = listOf(
                    "https://picsum.photos/200/300",
                    "https://picsum.photos/200/301",
                    "https://picsum.photos/200/302"
                ),
                videoThumbnail = "https://cdn-icons-png.flaticon.com/512/1384/1384060.png"
            )
        }
    }
}

@Composable
fun ExamActionColumn() {
    Column(
        modifier = Modifier.width(120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ", color = Color.White, fontSize = 14.sp)
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("15 من 20", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("مشاهدة الإجابات", color = Color.White, fontSize = 13.sp)
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFEAEAEA),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("45 دقيقة", fontSize = 14.sp)
            }
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFEAEAEA),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text("مستبعد", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun ExamStudentInfoCard(name: String, imageUrl: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Student Photo",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 3.dp
        ) {
            Text(
                text = name,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun ExamDetailsSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        InfoRow("الدرجة المستحقة:", "15 / 20")
        InfoRow("حالة الإجابات:", "مكتملة")
        InfoRow("الوقت الكلي للمحاولة:", "45 دقيقة")
        InfoRow("الحالة:", "مستبعد")
    }
}

@Composable
fun CheatingLogsSection(logs: List<String>) {
    Column(horizontalAlignment = Alignment.End) {
        Text("محاولات الغش:", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE9F2FF),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                logs.forEach { log ->
                    Text(log, fontSize = 14.sp, color = AppTextSecondary, textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun ExamMediaSection(imageUrls: List<String>, videoThumbnail: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text("لقطات المراقبة (صور):", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            imageUrls.reversed().forEach { // قلب ترتيب الصور ليتماشى مع الاتجاه RTL
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFDDE9FF)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Text("لقطات المراقبة (فيديو):", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        AsyncImage(
            model = videoThumbnail,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFDDE9FF)),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End)
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 15.sp, color = AppTextSecondary, textAlign = TextAlign.End)
    }
}

@Preview(showSystemUi = true, showBackground = true, locale = "ar")
@Composable
fun PreviewTeacherStudentExamScreen() {
    SaffiEDUAppTheme {
        TeacherStudentExamScreen()
    }
}
