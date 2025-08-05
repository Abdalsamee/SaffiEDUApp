package com.example.saffieduapp.presentation.screens.student.subjects

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen() {

    Scaffold(
        topBar = {
            // ٢. وضع الـ AppBar العام في مكانه المخصص
            CommonTopAppBar(
                title = "المواد الدراسية",
            )
        }
    ) { innerPadding ->
        // ٣. وضع محتوى الشاشة وتطبيق الـ padding
        // innerPadding تضمن أن المحتوى يبدأ أسفل الـ TopAppBar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Subject Screen Content")
        }
    }
}