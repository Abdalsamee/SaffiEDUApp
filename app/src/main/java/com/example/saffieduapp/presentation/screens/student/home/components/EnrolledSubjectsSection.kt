package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.domain.model.Subject
import com.example.saffieduapp.presentation.screens.student.components.HomeSubjectCard

@Composable
fun EnrolledSubjectsSection(
    subjects: List<Subject>,
    modifier: Modifier = Modifier,
    onSubjectClick: (SubjectId: String) -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "المواد",
                fontSize = 16.sp,
                color = Color.Black
            )

            Text(
                text = "المزيد",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.clickable {
                    onMoreClick()
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // التحقق مما إذا كانت قائمة المواد فارغة
        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "لا توجد مواد مسجلة حالياً",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(subjects) { subject ->
                    HomeSubjectCard(
                        subject = subject,
                        onClick = { onSubjectClick(subject.id) }
                    )
                }
            }
        }
    }
}
