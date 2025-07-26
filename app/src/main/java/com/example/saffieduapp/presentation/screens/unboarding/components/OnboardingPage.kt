package com.example.saffieduapp.presentation.screens.onboarding

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.onboarding.components.CurvedImageOnly
import com.example.saffieduapp.presentation.screens.unboarding.model.OnboardingPageData
import com.example.saffieduapp.ui.theme.Typography

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OnboardingPage(page: OnboardingPageData) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val imageSectionWeight = 0.6f
        val textSectionWeight = 0.4f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ عرض الصورة فقط (داخل الشكل المنحني الثابت مسبقًا)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(imageSectionWeight),
                contentAlignment = Alignment.TopCenter
            ) {
                CurvedImageOnly(
                    imageRes = page.imageRes
                )
            }

            // ✅ النصوص
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(textSectionWeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = page.title,
                    style = Typography.titleLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = page.description,
                    style = Typography.bodyMedium,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}
