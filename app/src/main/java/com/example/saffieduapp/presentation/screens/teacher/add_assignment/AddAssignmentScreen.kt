package com.example.saffieduapp.presentation.screens.teacher.add_assignment

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.add_assignment.components.ImagePickerBox
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.AddLessonTextField
import com.example.saffieduapp.presentation.screens.teacher.add_lesson.components.LessonDatePicker
import com.example.saffieduapp.presentation.screens.teacher.components.AppButton
import com.example.saffieduapp.presentation.screens.teacher.components.ClassDropdown
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme
import java.util.Locale

@Composable
fun AddAssignmentScreen(
    onNavigateUp: () -> Unit,
    viewModel: AddAssignmentViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    // الاستماع لأحداث Toast
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    val state by viewModel.state.collectAsState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            viewModel.onEvent(AddAssignmentEvent.ImageSelected(uri))
        }
    )
    AddAssignmentScreenContent(
        state = state,
        onNavigateUp = onNavigateUp,
        onTitleChange = { viewModel.onEvent(AddAssignmentEvent.TitleChanged(it)) },
        onDescriptionChange = { viewModel.onEvent(AddAssignmentEvent.DescriptionChanged(it)) },
        onClassSelected = { viewModel.onEvent(AddAssignmentEvent.ClassSelected(it)) },
        onDateSelected = { viewModel.onEvent(AddAssignmentEvent.DateChanged(it)) },
        onSaveClick = { viewModel.onEvent(AddAssignmentEvent.SaveClicked) },
        onImagePickerClick = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onClearImageClick = {
            viewModel.onEvent(AddAssignmentEvent.ImageSelected(null))
        }

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAssignmentScreenContent(
    state: AddAssignmentState,
    onNavigateUp: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onClassSelected: (String) -> Unit,
    onDateSelected: (String) -> Unit,
    onSaveClick: () -> Unit,
    onImagePickerClick: () -> Unit,
    onClearImageClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "إضافة واجب",
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AddLessonTextField(
                title = "إضافة عنوان",
                value = state.title,
                onValueChange = onTitleChange,
                placeholder = "أدخل عنوان الواجب ...",
                singleLine = true // عنوان الواجب عادةً ما يكون سطرًا واحدًا
            )

            AddLessonTextField(
                title = "إضافة وصف",
                // --- ٢. خطأ بسيط: كان يجب أن يقرأ من state.description ---
                value = state.description,
                onValueChange = onDescriptionChange,
                placeholder = "أدخل نص هذا الواجب ...",
                modifier = Modifier.height(150.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f), // weight لتقسيم المساحة
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "إضافة الصف", fontWeight = FontWeight.Normal, fontSize = 18.sp)
                    ClassDropdown(
                        selectedClass = state.selectedClass,
                        onClassSelected = onClassSelected, // ٣. ربط اختيار الصف
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "إضافة تاريخ التسليم", fontWeight = FontWeight.Normal, fontSize = 18.sp)
                    LessonDatePicker(
                        selectedDate = state.dueDate,
                        onDateSelected = { millis ->
                            // تحويل Long إلى String بصيغة yyyy-MM-dd
                            val formatted = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                                .format(java.util.Date(millis))
                            onDateSelected(formatted)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                }
            }
            ImagePickerBox(
                title = "إضافة صورة",
                selectedImageUri = state.selectedImageUri,
                onImagePickerClick = onImagePickerClick,
                onClearClick = onClearImageClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.weight(1f))

            AppButton(
                text = if (state.isSaving) "جار الحفظ..." else "حفظ ونشر للطلاب",
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving // تعطيل الزر أثناء الحفظ
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
private fun AddAssignmentScreenPreview() {
    SaffiEDUAppTheme {
        AddAssignmentScreenContent(
            state = AddAssignmentState(teacherName = "أ. طاهر زياد قديح"),
            onNavigateUp = {},
            onTitleChange = {},
            onDescriptionChange = {},
            onClassSelected = {},
            onDateSelected = {},
            onSaveClick = {},
            onImagePickerClick = {},
            onClearImageClick = {}
        )
    }
}