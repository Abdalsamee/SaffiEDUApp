package com.example.saffieduapp.presentation.screens.teacher.tasks.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.saffieduapp.navigation.Routes
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.student.home.components.SearchBar
import com.example.saffieduapp.presentation.screens.teacher.tasks.details.components.StudentTaskItemCard
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun TeacherTaskDetailsScreen(
    navController: NavController,
    viewModel: TeacherTaskDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "تفاصيل المهمة",
                onNavigateUp = {
                    navController.popBackStack()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            SearchBar(
                query = state.searchQuery,
                onQueryChanged = { viewModel.onSearchChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )


            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.students) { student ->
                        StudentTaskItemCard(
                            name = student.name,
                            score = student.score,
                            imageUrl = student.imageUrl,
                            onDetailsClick = {

                                navController.navigate("${Routes.TEACHER_STUDENT_EXAM_SCREEN}/${student.id}")
                                //navController.navigate(Routes.TEACHER_STUDENT_ASSIGNMENT_SCREEN)
                            }
                        )
                    }
                }
            }
        }
    }
}



