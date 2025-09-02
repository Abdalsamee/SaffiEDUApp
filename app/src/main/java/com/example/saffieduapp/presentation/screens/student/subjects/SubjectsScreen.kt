package com.example.saffieduapp.presentation.screens.student.subjects

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.subjects.component.SubjectListItemCard
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    navController: NavHostController,
    onNavigateToSubjectDetails: (subjectId: String) -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // حالة القائمة للتحكم بالتمرير
    val listState = rememberLazyListState()

    // حالة السحب للتحديث
    val pullToRefreshState = rememberPullToRefreshState()

    // التقاط إعادة اختيار تبّ "المواد" من الـ BottomBar
    LaunchedEffect(Unit) {
        val entry = navController.getBackStackEntry(Routes.SUBJECTS_SCREEN)
        entry.savedStateHandle
            .getStateFlow("tab_reselected_tick", 0L)
            .collectLatest { tick ->
                if (tick != 0L) {
                    listState.animateScrollToItem(0)
                    viewModel.refresh()
                }
            }
    }

    // إدارة دورة السحب للتحديث
    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(Unit) { viewModel.refresh() }
    }
    LaunchedEffect(state.isRefreshing) {
        if (!state.isRefreshing) pullToRefreshState.endRefresh()
    }

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "المواد الدراسية",
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppPrimary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.subject) { subject ->
                        SubjectListItemCard(
                            subject = subject,
                            onClick = {
                                println("SubjectsScreen: Navigating with ID -> ${subject.id}")
                                onNavigateToSubjectDetails(subject.id) },
                            onRatingChanged = { newRating ->
                                viewModel.updateRating(subject.id, newRating)
                                Toast.makeText(
                                    context,
                                    "تم تقييم المادة ${subject.name} بـ $newRating نجوم",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }

            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = AppPrimary
            )
        }
    }
}
