package com.example.saffieduapp.presentation.screens.teacher.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.presentation.screens.teacher.home.TopStudent

@Composable
fun TopStudentsSection(
    classes: List<String>,
    selectedClass: String?,
    topStudents: List<TopStudent>,
    onClassSelected: (String) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // الصف العلوي (المتفوقون + القائمة + المزيد)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // مجموعة (المتفوقون + القائمة)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "المتفوقون:", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp)) // مسافة بسيطة بين النص والقائمة
                if (selectedClass != null) {
                    ClassFilterDropdown(
                        classes = classes,
                        selectedClass = selectedClass,
                        onClassSelected = onClassSelected,
                      modifier = Modifier.size(width = 165.dp, height = 55.dp)
                    )
                }
            }

            // كلمة المزيد
            Text(
                text = "المزيد",
                fontSize = 16.sp,
                modifier = Modifier.clickable { onMoreClick() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // قائمة الطلاب المتفوقين (لاحقًا تضاف البطاقات)
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            topStudents.take(3).forEach { student ->
                TopStudentCard(student = student)
            }
        }
    }
}
