package com.example.saffieduapp.presentation.screens.student.subject_details.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.*

// ✅ بطاقة الـ PDF
@Composable
fun PdfCard(
    pdfLesson: PdfLesson,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // --- الصف العلوي: الصورة والعناوين ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = pdfLesson.imageUrl,
                        contentDescription = pdfLesson.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.rectangle),
                        error = painterResource(id = R.drawable.rectangle)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = pdfLesson.title,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = AppTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = pdfLesson.subTitle,
                        color = AppTextPrimary,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- الصف الأوسط: عدد الصفحات وزر الفتح ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عدد الصفحات ${pdfLesson.pagesCount}",
                    fontSize = 10.sp,
                    color = AppTextPrimary
                )

                Button(
                    onClick = {
                        println("DEBUG: Button in PdfCard was clicked!")
                        onClick()
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "فتح الملف", fontSize = 12.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ReadStatus(isRead = pdfLesson.isRead)
        }
    }
}

// ✅ حالة المقروء/غير المقروء
@Composable
private fun ReadStatus(isRead: Boolean) {
    val statusText = if (isRead) "مقروء" else "غير مقروء"
    val icon = if (isRead) Icons.Default.CheckBox else Icons.Filled.Square
    val iconColor = if (isRead) AppAccent else Color.White

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = statusText,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = AppTextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

// ✅ معاينة
@Preview(showBackground = true, name = "PDF Card - Unread")
@Composable
private fun PdfCardUnreadPreview() {
    val samplePdf = PdfLesson(
        id = "1",
        title = "ملخص الوحدة الأولى",
        subTitle = "النحو والصرف",
        pagesCount = 12,
        isRead = false,
        imageUrl = "",
        pdfUrl = "https://example.com/sample.pdf"
    )
    SaffiEDUAppTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .width(220.dp)
            ) {
                PdfCard(pdfLesson = samplePdf, onClick = {})
            }
        }
    }
}