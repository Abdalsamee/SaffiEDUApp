package com.example.saffieduapp.presentation.screens.student.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.profile.components.AcademicInfoCard
import com.example.saffieduapp.presentation.screens.student.profile.components.ProfileInfoCard
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onLogoutNavigate: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // 🔹 لفتح المعرض واختيار صورة
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateProfileImage(it)
        }
    }
    // 🔹 عرض الرسائل التوضيحية (نجاح / فشل)
    LaunchedEffect(state.message) {
        state.message?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { CommonTopAppBar(title = "الملف الشخصي") }
    ) { innerPadding ->

        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            StudentProfileContent(
                state = state,
                onEditPhoto = { imagePickerLauncher.launch("image/*") }, // ← فتح المعرض
                onLogoutClick = {
                    viewModel.logout {
                        onLogoutNavigate() // ← بعد تسجيل الخروج، ننتقل لتسجيل الدخول
                    }
                },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun StudentProfileContent(
    state: StudentProfileState,
    onEditPhoto: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔹 صورة الطالب مع أيقونة التعديل
        Box(contentAlignment = Alignment.BottomCenter) {
            AsyncImage(
                model = state.profileImageUrl ?: R.drawable.user,
                contentDescription = "Student Image",
                placeholder = painterResource(R.drawable.secstudent),
                error = painterResource(R.drawable.secstudent),
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            )

            // دائرة القلم الزرقاء فوق الصورة
            Box(
                modifier = Modifier
                    .offset(y = 15.dp)
                    .size(40.dp)
                    .background(AppPrimary, CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Photo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onEditPhoto() }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 الاسم والبريد الإلكتروني
        Text(
            text = "الطالب: ${state.fullName}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = state.email,
            color = Color.Gray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔸 عنوان القسم
        Text(
            text = "معلومات الحساب",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 بطاقات المعلومات
        ProfileInfoCard(
            label = "الاسم الكامل:",
            value = state.fullName,
            icon = R.drawable.user
        )
        ProfileInfoCard(
            label = "البريد الإلكتروني:",
            value = state.email,
            icon = R.drawable.email
        )
        ProfileInfoCard(
            label = "رقم الهوية:",
            value = state.phoneNumber,
            icon = R.drawable.idcard
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔸 عنوان القسم الثاني
        Text(
            text = "المعلومات الأكاديمية",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 صف بطاقتين (المعدل والصف الدراسي)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AcademicInfoCard(
                icon = R.drawable.graduationcap,
                label = "المعدل:",
                value = "${state.average} %",
                modifier = Modifier.weight(1f)
            )
            AcademicInfoCard(
                icon = R.drawable.profclass,
                label = "الصف:",
                value = state.className,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start // ✅ محاذاة إلى البداية
        ) {
            // 🔹 زر تسجيل الخروج
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = AppAlert),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(48.dp)

            ) {
                Text(text = "تسجيل الخروج", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }


        Spacer(modifier = Modifier.weight(1f))
    }
}


//@Preview(showBackground = true, locale = "ar", showSystemUi = true)
//@Composable
//private fun PreviewStudentProfileScreen() {
//    SaffiEDUAppTheme {
//        StudentProfileContent(
//            state = StudentProfileState(
//                isLoading = false,
//                fullName = "فرج النجار",
//                email = "faragstudent123@gmail.com",
//                phoneNumber = "1234567890",
//                className = "الثاني عشر",
//                average = "96",
//                profileImageUrl = null
//            ),
//            onEditPhoto = {},
//            onLogoutClick = {}
//        )
//
//    }
//}
