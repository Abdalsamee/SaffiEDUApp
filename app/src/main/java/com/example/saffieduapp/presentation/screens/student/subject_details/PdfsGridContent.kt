package com.example.saffieduapp.presentation.screens.student.subject_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.presentation.screens.student.subject_details.components.PdfCard
import com.example.saffieduapp.presentation.screens.student.subject_details.components.PdfLesson

@Composable
fun PdfsGridContent(
    pdfs: List<PdfLesson>,
    onReadStatusChange: (pdfLesson: PdfLesson, isRead: Boolean) -> Unit
) {
    if (pdfs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("لا توجد ملخصات متاحة حاليًا.")
        }
        return
    }

    // <--- تعديل: استخدمنا LazyVerticalGrid بنفس الطريقة السابقة
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = pdfs,
            key = { pdf -> pdf.id }
        ) { pdfLesson ->
            // استدعاء بطاقة الـ PDF مباشرةً
            PdfCard(
                pdfLesson = pdfLesson,

            )
        }
    }
}