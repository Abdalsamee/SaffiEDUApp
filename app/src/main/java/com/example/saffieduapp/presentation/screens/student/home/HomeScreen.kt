package com.example.saffieduapp.presentation.screens.student.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.home.components.*
import com.example.saffieduapp.ui.theme.AppPrimary
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val systemUiController = rememberSystemUiController()
    val statusBarColor = AppPrimary
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = false
        )
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

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
                UrgentTasksSection(tasks = state.urgentTasks)
            }
            item {
                EnrolledSubjectsSection(subjects = state.enrolledSubjects)
            }
            item {
                FeaturedLessonsSection(lessons = state.featuredLessons)
            }
        }


        HomeTopSection(
            studentName = state.studentName,
            studentGrade = state.studentGrade,
            profileImageUrl = state.profileImageUrl,

        )


        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}