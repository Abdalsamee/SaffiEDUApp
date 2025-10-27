package com.example.saffieduapp.presentation.screens.teacher.profile

import android.net.Uri
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.profile.components.ProfileInfoCard
import com.example.saffieduapp.presentation.screens.student.profile.components.AcademicInfoCard
import com.example.saffieduapp.ui.theme.AppAlert
import com.example.saffieduapp.ui.theme.AppPrimary
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@Composable
fun TeacherProfileScreen(
    viewModel: TeacherProfileViewModel = hiltViewModel(), onLogoutNavigate: () -> Unit, // ✅ أضف هذا
    navController: NavHostController

) {

    // ✅ 1. إعداد مُشغّل اختيار الصور
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // عند اختيار المستخدم لصورة (uri لا يساوي null)
        uri?.let {
            viewModel.updateProfilePhoto(it)
        }
    }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { CommonTopAppBar(title = "الملف الشخصي") }) { innerPadding ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.error ?: "حدث خطأ غير معروف",
                        color = AppAlert,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            else -> {
                TeacherProfileContent(
                    state = state, onEditPhoto = {
                    imagePickerLauncher.launch("image/*")
                }, onLogoutClick = {
                    viewModel.logout {
                        onLogoutNavigate()
                    }
                }, modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun TeacherProfileContent(
    state: TeacherProfileState,
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

        // 🔹 صورة المعلم مع أيقونة التعديل
        Box(contentAlignment = Alignment.BottomCenter) {
            AsyncImage(
                model = state.profileImageUrl ?: R.drawable.fullname,
                contentDescription = "Teacher Image",
                placeholder = painterResource(R.drawable.fullname),
                error = painterResource(R.drawable.fullname),
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            )

            Box(
                modifier = Modifier
                    .offset(y = 15.dp)
                    .size(40.dp)
                    .background(AppPrimary, CircleShape)
                    .border(3.dp, Color.White, CircleShape)
                    .padding(6.dp), contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Photo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onEditPhoto() })
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 الاسم والبريد الإلكتروني
        Text(
            text = "أ. ${state.fullName}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Text(
            text = state.email, color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔸 القسم الأول: معلومات الحساب
        Text(
            text = "معلومات الحساب",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProfileInfoCard(
            label = "الاسم الكامل :", value = state.fullName, icon = R.drawable.user
        )
        ProfileInfoCard(
            label = "البريد الإلكتروني :", value = state.email, icon = R.drawable.email
        )
        ProfileInfoCard(
            label = "رقم الهوية :", value = state.nationalId, icon = R.drawable.idcard
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔸 القسم الثاني: المعلومات الأكاديمية
        Text(
            text = "المعلومات الأكاديمية",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AcademicInfoCard(
                icon = R.drawable.subjecticon,
                label = "المادة :",
                value = state.subject,
                modifier = Modifier.weight(1f)
            )
            AcademicInfoCard(
                icon = R.drawable.profclass,
                label = "الصفوف :",
                value = "${state.classesCount} صفوف",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 🔹 زر تسجيل الخروج
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onLogoutClick,// ← تعود إلى شاشة تسجيل الدخول
                colors = ButtonDefaults.buttonColors(containerColor = AppAlert),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(48.dp)
            ) {
                Text(
                    text = "تسجيل الخروج", fontWeight = FontWeight.SemiBold, color = Color.White
                )
            }
        }
    }
}