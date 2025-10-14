package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components.StudentHeaderRow
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherStudentExamScreen(
    navController: NavController? = null,
    // لاحقًا ستمرر القيم الحقيقية من الـ ViewModel
    studentName: String = "يزن عادل ظهير",
    studentImageUrl: String = "https://randomuser.me/api/portraits/men/60.jpg",
    onSaveClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "نظام المراقبة",
                onNavigateUp = { navController?.popBackStack() }
            )
        }
    ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // الصف المطلوب: صورة → بطاقة اسم → زر حفظ
                StudentHeaderRow(
                    studentName = studentName,
                    studentImageUrl = studentImageUrl,
                    onSaveClick = onSaveClick
                )

            }
        }
    }




@Preview(showBackground = true, showSystemUi = true, locale = "ar")
@Composable
private fun PreviewTeacherStudentExamScreen_HeaderOnly() {
    SaffiEDUAppTheme {
        TeacherStudentExamScreen()
    }
}
