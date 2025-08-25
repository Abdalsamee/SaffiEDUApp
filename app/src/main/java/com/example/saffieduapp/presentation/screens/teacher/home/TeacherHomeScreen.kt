package com.example.saffieduapp.presentation.screens.teacher.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.home.components.HomeTopSection
import com.example.saffieduapp.presentation.screens.student.home.components.SearchBar
import com.example.saffieduapp.presentation.screens.teacher.home.component.ClassFilterDropdown
import com.example.saffieduapp.presentation.screens.teacher.home.component.TeacherClassesSection
import com.example.saffieduapp.presentation.screens.teacher.home.component.TopStudentsSection
import com.example.saffieduapp.presentation.screens.teacher.home.components.StudentUpdatesSection
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun TeacherHomeScreen(
    viewModel: TeacherHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // ١. Box الرئيسي يبدأ هنا
    Box(modifier = Modifier.fillMaxSize()) {

        // ٢. المحتوى القابل للتمرير (الطبقة السفلية)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 130.dp)
        ) {
            item {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged
                )
            }

            item {
                StudentUpdatesSection(
                    updates = state.studentUpdates,
                    onUpdateClick = { /* TODO */ },
                    onMoreClick = { /* TODO */ },
                    onLoadMore = { /* viewModel.loadNextUpdates() */ }
                )
            }
            item {
                TeacherClassesSection(
                    classes = state.teacherClasses,
                    subjectName = state.teacherRole,
                    onClassClick = { classId ->
                        // TODO: Handle click on a specific class
                    },
                    onMoreClick = {
                        // TODO: Handle click on "More"
                    }
                )
            }


            item {
                TopStudentsSection(
                    classes = state.availableClassesForFilter,
                    selectedClass = state.selectedClassFilter,
                    topStudents = state.topStudents,
                    onClassSelected = viewModel::onClassFilterSelected,
                    onMoreClick = {
                        // TODO: Handle click on "More"
                    }
                )
            }
        }



        HomeTopSection(
            studentName = state.teacherName,
            studentGrade = state.teacherRole,
            profileImageUrl = state.profileImageUrl,

        )


        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPrimary)
            }
        }
    }
}