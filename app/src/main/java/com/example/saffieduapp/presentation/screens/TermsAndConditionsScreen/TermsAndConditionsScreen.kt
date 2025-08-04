package com.example.saffieduapp.presentation.screens.terms

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TermsAndConditionsScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var isChecked by remember { mutableStateOf(true) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        // تحويل maxWidth و maxHeight إلى Float لاستخدام النسب
        val width = maxWidth.value
        val height = maxHeight.value

        val iconBoxSize = (width * 0.28f).dp
        val iconSize = (width * 0.15f).dp
        val buttonHeight = (height * 0.065f).dp
        val titleFontSize = (width * 0.06f).sp
        val bodyFontSize = (width * 0.04f).sp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ===== العنوان =====
            Text(
                text = "الشروط والأحكام",
                style = Typography.titleLarge.copy(
                    color = AppTextPrimary,
                    fontSize = titleFontSize
                ),
                modifier = Modifier.padding(vertical = (height * 0.02f).dp)
            )

            // ===== أيقونة داخل بوكس دائري =====
            Box(
                modifier = Modifier
                    .size(iconBoxSize)
                    .background(AppPrimary.copy(alpha = 0.1f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_terms),
                    contentDescription = "الشروط",
                    modifier = Modifier.size(iconSize)
                )
            }

            Spacer(modifier = Modifier.height((height * 0.03f).dp))

            // ===== نص الشروط =====
            Text(
                text = "يرجى قراءة هذه الشروط بعناية قبل استخدام تطبيقنا للتعليم.\n\n" +
                        "من خلال استخدام التطبيق، المستخدم يوافق على هذه الشروط ويقر بأنه قرأها وفهمها جيدًا.\n\n" +
                        "إذا كنت لا توافق على هذه الشروط، يرجى عدم استخدام التطبيق.\n\n" +
                        "قد نقوم بتحديث هذه الشروط من وقت لآخر، وسيتم إخطارك بأي تغييرات.",
                style = Typography.bodyLarge.copy(
                    color = AppTextSecondary,
                    fontSize = bodyFontSize
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (width * 0.02f).dp)
            )

            Spacer(modifier = Modifier.height((height * 0.02f).dp))

            // ===== Checkbox =====
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (width * 0.02f).dp)
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppPrimary
                    )
                )
                Text(
                    text = "لقد قرأت هذه الشروط وأوافق على استخدامها",
                    style = Typography.bodyMedium.copy(color = AppTextPrimary, fontSize = bodyFontSize)
                )
            }

            Spacer(modifier = Modifier.height((height * 0.04f).dp))

            // ===== الأزرار =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (width * 0.01f).dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onDecline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD6D6D6)),
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                        .padding(end = (width * 0.015f).dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("لا أوافق", color = AppTextPrimary, fontSize = bodyFontSize)
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                        .padding(start = (width * 0.015f).dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("أوافق", color = AppBackground, fontSize = bodyFontSize)
                }
            }
        }
    }
}
