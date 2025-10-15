package com.example.saffieduapp.presentation.screens.student.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun StudentProfileScreen(
    viewModel: StudentProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { CommonTopAppBar(title = "الملف الشخصي") }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.errorMessage ?: "خطأ غير معروف", color = MaterialTheme.colorScheme.error)
                }
            }

            else -> {
                StudentProfileContent(
                    state = state,
                    onLogoutClick = viewModel::logout,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun StudentProfileContent(
    state: StudentProfileState,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // الصورة الشخصية
        if (state.profileImageUrl != null) {
            AsyncImage(
                model = state.profileImageUrl,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "Default Image",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(16.dp)
            )
        }

        Text(
            text = state.fullName,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(text = state.email, fontSize = 15.sp)

        Divider(thickness = 1.dp)

        ProfileInfoRow(label = "رقم الجوال:", value = state.phoneNumber)
        ProfileInfoRow(label = "الصف:", value = state.className)
        ProfileInfoRow(label = "المعدل:", value = "${state.average}%")

        Spacer(modifier = Modifier.height(20.dp))

        AppButton(text = "تسجيل الخروج", onClick = onLogoutClick)
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Medium)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun PreviewStudentProfileScreen() {
    SaffiEDUAppTheme {
        StudentProfileContent(
            state = StudentProfileState(
                isLoading = false,
                fullName = "فرج النجار",
                email = "faragstudent123@gmail.com",
                phoneNumber = "1234567890",
                className = "الثاني عشر",
                average = "96",
                profileImageUrl = null
            ),
            onLogoutClick = {}
        )
    }
}
