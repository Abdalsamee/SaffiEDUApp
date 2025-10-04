package com.example.saffieduapp.presentation.screens.teacher.calsses

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.calsses.components.TeacherClassCard
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassesScreen(
    viewModel: TeacherClassesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)

     fun extractClassNumber(className: String): Int {
        return when {
            className.contains("الثاني عشر") -> 12
            className.contains("الحادي عشر") -> 11
            className.contains("العاشر") -> 10
            className.contains("التاسع") -> 9
            className.contains("الثامن") -> 8
            className.contains("السابع") -> 7
            className.contains("السادس") -> 6
            className.contains("الخامس") -> 5
            className.contains("الرابع") -> 4
            className.contains("الثالث") -> 3
            className.contains("الثاني") -> 2
            className.contains("الأول") -> 1
            else -> 0
        }
    }
    Scaffold(
        topBar = { CommonTopAppBar(title = "صفوفي") }
    ) { innerPadding ->

        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.refreshClasses() }, // هنا سحب الشاشة للأسفل
            modifier = Modifier.padding(innerPadding)
        ) {
            when {
                state.isLoading && !state.isRefreshing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 180.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.classes.sortedBy { extractClassNumber(it.className) }) { classItem ->
                            TeacherClassCard(
                                classItem = classItem,
                                onClick = {
                                    Toast.makeText(
                                        context,
                                        "Clicked on ${classItem.className}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
