package com.example.saffieduapp.presentation.screens.teacher.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.teacher.home.StudentUpdate
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentUpdatesSection(
    updates: List<StudentUpdate>,
    modifier: Modifier = Modifier,
    onUpdateClick: (studentId: String) -> Unit,
    onMoreClick: () -> Unit,
    onLoadMore: () -> Unit
) {
    // لا نعرض القسم إذا كانت القائمة فارغة
    if (updates.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // عنوان القسم
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "مستجدات الطالب", fontSize = 16.sp, color = Color.Black)
            Text(
                text = "المزيد",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.clickable { onMoreClick() }
            )
        }

        // ١. تعريف Pager لانهائي واحد فقط
        val pagerState = rememberPagerState(
            initialPage = Int.MAX_VALUE / 2,
            pageCount = { Int.MAX_VALUE }
        )

        // ٢. تأثير التمرير التلقائي المستمر
        LaunchedEffect(Unit) {
            while (true) {
                delay(5000) // انتظر 5 ثوانٍ
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }

        // ٣. تأثير طلب المزيد من البيانات عند الوصول للنهاية
        LaunchedEffect(pagerState.currentPage) {
            val actualIndex = pagerState.currentPage % updates.size
            if (actualIndex == updates.size - 1) {
                onLoadMore()
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp,
        ) { page ->
            // ٤. استخدام باقي القسمة (%) لعرض البيانات الصحيحة
            val actualIndex = page % updates.size
            StudentUpdateCard(
                update = updates[actualIndex],
                onClick = { onUpdateClick(updates[actualIndex].studentId) }
            )
        }

        // مؤشر الصفحات (النقاط)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(updates.size) { index ->
                val color = if (pagerState.currentPage % updates.size == index) AppPrimary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}