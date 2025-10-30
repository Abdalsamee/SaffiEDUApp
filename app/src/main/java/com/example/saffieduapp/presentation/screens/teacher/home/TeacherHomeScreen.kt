package com.example.saffieduapp.presentation.screens.teacher.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.navigation.Routes
import com.example.saffieduapp.presentation.screens.student.home.components.HomeTopSection
import com.example.saffieduapp.presentation.screens.student.home.components.SearchBar
import com.example.saffieduapp.presentation.screens.teacher.home.component.TeacherClassesSection
import com.example.saffieduapp.presentation.screens.teacher.home.component.TopStudentsSection
import com.example.saffieduapp.presentation.screens.teacher.home.components.StudentUpdatesSection
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun TeacherHomeScreen(
    viewModel: TeacherHomeViewModel = hiltViewModel(), navController: NavController
) {
    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // ⭐️ إضافة جديدة: الاستماع للأحداث القادمة من الـ ViewModel
    LaunchedEffect(key1 = true) { // key1 = true ليعمل مرة واحدة عند بدء الشاشة
        viewModel.eventFlow.collect { event ->
            when (event) {
                is TeacherHomeViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
            }
        }
    }

    // ١. Box الرئيسي يبدأ هنا
    Box(modifier = Modifier.fillMaxSize()) {

        // ٢. المحتوى القابل للتمرير (الطبقة السفلية)
        LazyColumn(
            modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 130.dp)
        ) {
            item {
                SearchBar(
                    query = state.searchQuery, onQueryChanged = viewModel::onSearchQueryChanged
                )
            }

            item {
                StudentUpdatesSection(
                    updates = state.studentUpdates,
                    onUpdateClick = { /* TODO */ },
                    onMoreClick = { /* TODO */ },
                    onLoadMore = { /* viewModel.loadNextUpdates() */ })
            }
            item {
                TeacherClassesSection(
                    classes = state.teacherClasses,
                    subjectName = state.teacherSub,
                    onClassClick = { classId ->
                        // TODO: Handle click on a specific class
                    },
                    onMoreClick = {
                        navController.navigate(Routes.TEACHER_CLASSES_SCREEN)
                    })
            }


            item {
                TopStudentsSection(
                    classes = state.availableClassesForFilter,
                    selectedClass = state.selectedClassFilter,
                    topStudents = state.topStudents,
                    onClassSelected = viewModel::onClassFilterSelected,
                    onMoreClick = {
                        // TODO: Handle click on "More"
                    })
            }
        }

        HomeTopSection(
            studentName = state.teacherName, // ✅ الاسم صار مفرمت من ViewModel
            studentSubject = state.teacherSub.removePrefix("مدرس ").trim(),
            profileImageUrl = state.profileImageUrl,
            showActivateButton = state.showActivateButton,
            onActivateClick = { viewModel.activateSubject() })


        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPrimary)
            }
        }
        // ⭐️⭐️⭐️ التعديل المطلوب: إضافة SnackbarHost ⭐️⭐️⭐️
        // هذا هو المكون الفعلي الذي يعرض رسائل الـ Snackbar
        // يجب ربطه بـ snackbarHostState الذي يتم التحكم به من الـ LaunchedEffect
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}