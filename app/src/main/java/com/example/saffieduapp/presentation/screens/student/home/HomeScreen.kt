package com.example.saffieduapp.presentation.screens.student.home

import android.widget.Toast
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
import com.example.saffieduapp.presentation.screens.student.home.components.EnrolledSubjectsSection
import com.example.saffieduapp.presentation.screens.student.home.components.FeaturedLessonsSection
import com.example.saffieduapp.presentation.screens.student.home.components.HomeTopSection
import com.example.saffieduapp.presentation.screens.student.home.components.SearchBar
import com.example.saffieduapp.presentation.screens.student.home.components.UrgentTasksSection
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSubjects: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // حالة التمرير لقائمة الصفحة الرئيسية
    val listState = rememberLazyListState()

    // سحب للتحديث
    val pullToRefreshState = rememberPullToRefreshState()

    // التقاط إعادة اختيار تبّ "الصفحة الرئيسية" من الـ BottomBar
    LaunchedEffect(Unit) {
        val entry = navController.getBackStackEntry(Routes.HOME_SCREEN)
        entry.savedStateHandle
            .getStateFlow("tab_reselected_tick", 0L)
            .collectLatest { tick ->
                if (tick != 0L) {
                    // رجوع لأعلى الواجهة
                    listState.animateScrollToItem(0)
                    // تحديث اختياري
                    viewModel.refresh()
                }
            }
    }

    // إدارة دورة السحب للتحديث
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.refresh()
        }
    }
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) pullToRefreshState.endRefresh()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // المحتوى القابل للتمرير أسفل الهيدر
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 130.dp) // ارتفاع الهيدر
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            item {
                SearchBar(
                    query = state.searchQuery,
                    onQueryChanged = viewModel::onSearchQueryChanged
                )
            }
            item {
                UrgentTasksSection(
                    tasks = state.urgentTasks,
                    onTaskClick = { taskId ->
                        Toast.makeText(context, "Clicked Task ID: $taskId", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            item {
                EnrolledSubjectsSection(
                    subjects = state.enrolledSubjects,
                    onSubjectClick = { subjectId ->
                        Toast.makeText(context, "Clicked Subject ID: $subjectId", Toast.LENGTH_SHORT).show()
                    },
                    onMoreClick = onNavigateToSubjects
                )
            }
            item {
                FeaturedLessonsSection(
                    lessons = state.featuredLessons,
                    onFeaturedLessonClick = { lessonId ->
                        Toast.makeText(context, "Clicked Lesson ID: $lessonId", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 120.dp),
            contentColor = AppPrimary
        )
        // الهيدر الثابت
        HomeTopSection(
            studentName = state.studentName,
            studentGrade = state.studentGrade,
            profileImageUrl = state.profileImageUrl,
        )


        // دائرة تحميل وسط الصفحة أثناء التحميل الأولي فقط
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