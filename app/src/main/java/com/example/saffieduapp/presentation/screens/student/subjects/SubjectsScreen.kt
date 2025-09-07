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
import androidx.navigation.NavController
import com.example.saffieduapp.navigation.Routes
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.subjects.component.SubjectListItemCard
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(
    // --- ١. قمنا بحذف NavController واكتفينا بالـ lambdas ---
    onNavigateToSubjectDetails: (subjectId: String) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SubjectsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    // ملاحظة: هذا الكود المتقدم للتعامل مع savedStateHandle يتطلب تمرير NavController
    // يمكنك إبقاؤه أو حذفه إذا كان يسبب مشاكل. سأبقيه الآن.
    // LaunchedEffect(Unit) { ... }

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
                // --- ٢. ربطنا دالة الرجوع للخلف ---
                onNavigateUp = onNavigateUp
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
                    items(state.subject) { subject -> // <-- قمت بتصحيح subject هنا
                        SubjectListItemCard(
                            subject = subject,
                            onClick = { onNavigateToSubjectDetails(subject.id) },
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