package com.example.saffieduapp.presentation.screens.student.subject_details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.home.components.SearchBar
import com.example.saffieduapp.presentation.screens.student.subject_details.components.LessonCard
import com.example.saffieduapp.presentation.screens.student.subject_details.components.PdfCard
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailsScreen(
    onNavigateUp: () -> Unit,
    viewModel: SubjectDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = state.subject?.name ?: "تفاصيل المادة",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. التبويبات (ثابتة ولا يتم تمريرها)
            SubjectTabsRow(
                selectedTab = state.selectedTab,
                onTabSelected = viewModel::onTabSelected
            )

            // 2. المحتوى القابل للتمرير
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // شريط البحث
                item {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChanged = viewModel::onSearchQueryChanged,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // قسم التنبيهات
                item {
                    // <--- تعديل: نقلنا العنوان إلى هنا ليظهر دائماً
                    Text(
                        text = "التنبيهات",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp)
                    )
                }

                if (state.alerts.isNotEmpty()) {
                    items(state.alerts) { alert ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = AppPrimary)
                        ) {
                            Text(
                                text = alert.message,
                                color = Color.White,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    // <--- إضافة: عرض رسالة في حال عدم وجود تنبيهات
                    item {
                        Text(
                            text = "لا توجد تنبيهات حالياً.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            color = AppTextSecondary
                        )
                    }
                }

                // محتوى التبويب المحدد
                when (state.selectedTab) {
                    SubjectTab.VIDEOS -> {
                        item {
                            Text(
                                text = "الدروس",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        if (state.videoLessons.isNotEmpty()) {
                            items(state.videoLessons.chunked(2)) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { lesson ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            LessonCard(lesson = lesson)
                                        }
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            // <--- إضافة: عرض رسالة في حال عدم وجود دروس
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize() // تملأ المساحة المتبقية في الشاشة
                                        .height(200.dp), // ارتفاع افتراضي
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "لا توجد دروس مصورة حالياً.",
                                        color = AppTextSecondary
                                    )
                                }
                            }
                        }
                    }
                    SubjectTab.PDFS -> {
                        item {
                            Text(
                                text = "الملخصات",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        if (state.pdfSummaries.isNotEmpty()) {
                            items(state.pdfSummaries.chunked(2)) { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { pdf ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            PdfCard(
                                                pdfLesson = pdf,
                                            )
                                        }
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            // <--- إضافة: عرض رسالة في حال عدم وجود ملخصات
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "لا توجد ملخصات PDF حالياً.",
                                        color = AppTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- باقي الـ Composables كما هي ---

@Composable
private fun SubjectTabsRow(
    selectedTab: SubjectTab,
    onTabSelected: (SubjectTab) -> Unit
) {
    // ... (الكود لم يتغير)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabItem(
            text = "الدروس المصوّرة",
            isSelected = selectedTab == SubjectTab.VIDEOS,
            onClick = { onTabSelected(SubjectTab.VIDEOS) }
        )
        TabItem(
            text = "الملخّصات PDF",
            isSelected = selectedTab == SubjectTab.PDFS,
            onClick = { onTabSelected(SubjectTab.PDFS) }
        )
    }
}

@Composable
private fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    // ... (الكود لم يتغير)
    val textColor = if (isSelected) AppPrimary else AppTextSecondary
    Text(
        text = text,
        color = textColor,
        fontWeight = FontWeight.Medium,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    )
}

@Composable
fun AlertsSection(alerts: List<Alert>, modifier: Modifier = Modifier) {
    // ... (الكود لم يتغير)
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "التنبيهات",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 3.dp)
        )
        alerts.forEach { alert ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppPrimary
                )
            ) {
                Text(
                    text = alert.message,
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}