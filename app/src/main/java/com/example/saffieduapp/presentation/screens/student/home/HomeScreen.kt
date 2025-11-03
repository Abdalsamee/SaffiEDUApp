package com.example.saffieduapp.presentation.screens.student.home

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.saffieduapp.navigation.Routes
import com.example.saffieduapp.navigation.navigateToVideoScreen
import com.example.saffieduapp.presentation.screens.student.home.components.*
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

@RequiresApi(Build.VERSION_CODES.Q)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSubjects: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val listState = rememberLazyListState()

    val pullToRefreshState = rememberPullToRefreshState()

// عند السحب للتحديث
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing && !state.isRefreshing) {
            viewModel.refresh()
        }
    }

// عند انتهاء التحديث في ViewModel
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing && pullToRefreshState.isRefreshing) {
            pullToRefreshState.endRefresh()
        }
    }
    // إعادة اختيار تب الصفحة الرئيسية
    LaunchedEffect(Unit) {
        val entry = navController.getBackStackEntry(Routes.HOME_SCREEN)
        entry.savedStateHandle.getStateFlow("tab_reselected_tick", 0L).collectLatest { tick ->
            if (tick != 0L) {
                listState.animateScrollToItem(0)
                viewModel.refresh()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 130.dp)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            item {
                SearchBar(
                    query = state.searchQuery, onQueryChanged = viewModel::onSearchQueryChanged
                )
            }
            item {
                // عرض فقط أول عنصرين من الواجبات
                UrgentTasksSection(tasks = state.urgentTasks.take(2), onTaskClick = { examId ->
                    navController.navigate(Routes.EXAM_DETAILS_SCREEN + "/$examId")
                }, onMoreClick = {
                    // عند الضغط على "عرض المزيد" انتقل إلى شاشة المهام الكاملة
                    navController.navigate(Routes.TASKS_SCREEN)
                })
            }


            item {
                EnrolledSubjectsSection(
                    subjects = state.enrolledSubjects, onSubjectClick = { subjectId ->
                        navController.navigate(Routes.SUBJECT_DETAILS_SCREEN + "/$subjectId")
                    }, onMoreClick = onNavigateToSubjects
                )
            }

            item {
                FeaturedLessonsSection(
                    lessons = state.featuredLessons, onFeaturedLessonClick = { lesson ->
                        navController.navigateToVideoScreen(
                            videoUrl = lesson.videoUrl
                        )
                    })
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
            contentColor = AppPrimary
        )

        HomeTopSection(
            studentName = state.studentName,
            studentSubject = state.studentGrade,
            profileImageUrl = state.profileImageUrl.toString(),
        )

        if (state.isLoading && !state.isRefreshing) {
            Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppPrimary)
            }
        }
    }
}
