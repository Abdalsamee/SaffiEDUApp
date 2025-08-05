package com.example.saffieduapp.presentation.screens.terms

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(16.dp)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        val iconBoxSize = 120.dp
        val iconSize = screenWidth * 0.15f
        val buttonHeight = screenHeight * 0.065f
        val titleFontSize = (screenWidth.value * 0.045f).sp
        val bodyFontSize = (screenWidth.value * 0.035f).sp
        val paddingHorizontal = screenWidth * 0.05f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // العنوان (في المنتصف)
            Text(
                text = "الشروط والأحكام",
                style = MaterialTheme.typography.displayLarge.copy(
                    color = AppTextPrimary,
                    fontSize = titleFontSize,
                    fontFamily = Cairo,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .padding(vertical = screenHeight * 0.02f)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // شعار مربع بحجم 120x120 مع خلفية شفافة بلون AppPrimary
            Box(
                modifier = Modifier
                    .size(iconBoxSize)
                    .background(AppPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_terms),
                    contentDescription = "الشروط",
                    modifier = Modifier.size(iconSize)
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.03f))

            // نصوص الشروط تبدأ من اليمين
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingHorizontal)
            ) {
                // الفقرة الفرعية الأولى الجديدة
                Text(
                    text = "يرجى قراءة هذه الشروط بعناية قبل استخدام تطبيقنا التعليمي"

                            ,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = AppTextPrimary,
                        fontSize = bodyFontSize,
                        fontFamily = Cairo,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(bottom = screenHeight * 0.015f)
                )

                // الفقرة الفرعية الثانية الجديدة
                Text(
                    text = "نحرص على توفير بيئة تعليمية تحترم الخصوصية، وتحفّز على التعلّم، وتلتزم بأعلى معايير الأمان والمحتوى التربوي.\n" +
                            "إذا كنت مستخدمًا دون سن 18 عامًا، يُرجى التأكد من مراجعة هذه الشروط بمساعدة أحد الوالدين أو ولي الأمر.\n" +
                            "استمرارك في استخدام التطبيق يعني أنك قرأت وفهمت ووافقت على كل ما ورد في هذه الشروط.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = AppTextSecondary,
                        fontSize = bodyFontSize,
                        fontFamily = Cairo,
                        fontWeight = FontWeight.Normal
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(bottom = screenHeight * 0.015f)
                )

                // النص الأصلي
                Text(
                    text = "باستخدامك للتطبيق، فإنك توافق على هذه الشروط وتقر بأنك قرأتها وفهمتها جيدًا.\n\n" +
                            "إذا كنت لا توافق على هذه الشروط، يرجى التوقف عن استخدام التطبيق.\n\n" +
                            "قد نقوم بتحديث هذه الشروط من وقت لآخر، وسيتم إخطارك بأي تغييرات.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = AppTextSecondary,
                        fontSize = bodyFontSize,
                        fontFamily = Cairo,
                        fontWeight = FontWeight.Normal
                    ),
                    textAlign = TextAlign.Right
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.04f))

            // الأزرار
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = screenWidth * 0.02f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onDecline,
                    colors = ButtonDefaults.buttonColors(containerColor = AppTextSecondary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                        .padding(end = screenWidth * 0.015f),
                    shape = RoundedCornerShape(screenWidth * 0.02f)
                ) {
                    Text(
                        "لا أوافق",
                        color = AppTextPrimary,
                        fontSize = bodyFontSize,
                        fontFamily = Cairo
                    )
                }
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight)
                        .padding(start = screenWidth * 0.015f),
                    shape = RoundedCornerShape(screenWidth * 0.02f)
                ) {
                    Text(
                        "أوافق",
                        color = AppBackground,
                        fontSize = bodyFontSize,
                        fontFamily = Cairo
                    )
                }
            }
        }
    }
}
