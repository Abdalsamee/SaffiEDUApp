package com.example.saffieduapp.presentation.screens.student.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.home.components.EnrolledSubjectsSection
import com.example.saffieduapp.presentation.screens.student.home.components.FeaturedLessonsSection
import com.example.saffieduapp.presentation.screens.student.home.components.HomeTopSection
import com.example.saffieduapp.presentation.screens.student.home.components.SearchBar
import com.example.saffieduapp.presentation.screens.student.home.components.UrgentTasksSection
import com.example.saffieduapp.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        val pullToRefreshState = rememberPullToRefreshState()

        if (pullToRefreshState.isRefreshing) {
            viewModel.refresh()
        }
        if (!state.isRefreshing) {
            pullToRefreshState.endRefresh()
        }

        // الهيدر الثابت
        HomeTopSection(
            studentName = state.studentName,
            studentGrade = state.studentGrade,
            profileImageUrl = state.profileImageUrl,
        )

        // محتوى السكرول
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 130.dp) // المسافة تحت الهيدر
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            item {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged
                )
            }
            item {
                UrgentTasksSection(tasks = state.urgentTasks, onTaskClick = { taskId ->
                    Toast.makeText(context, "Clicked Task ID: $taskId", Toast.LENGTH_SHORT).show()
                })
            }
            item {
                EnrolledSubjectsSection(subjects = state.enrolledSubjects, onSubjectClick = { subjectId ->
                    Toast.makeText(context, "Clicked Subject ID: $subjectId", Toast.LENGTH_SHORT).show()
                })
            }
            item {
                FeaturedLessonsSection(lessons = state.featuredLessons, onFeaturedLessonClick = { lessonId ->
                    Toast.makeText(context, "Clicked Lesson ID: $lessonId", Toast.LENGTH_SHORT).show()
                })
            }
        }

        // مؤشر التحديث (Pull to Refresh)
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            contentColor = AppPrimary
        )

        // مؤشر التحميل الأولي
        if (state.isLoading && !state.isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPrimary)
            }
        }
    }
}
