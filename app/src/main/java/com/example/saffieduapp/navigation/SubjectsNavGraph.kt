package com.example.saffieduapp.navigation


import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.example.saffieduapp.presentation.screens.student.subject_details.SubjectDetailsScreen
import com.example.saffieduapp.presentation.screens.student.subjects.SubjectsScreen

fun NavGraphBuilder.subjectsNavGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.SUBJECTS_LIST_SCREEN,
        route = Routes.SUBJECTS_SCREEN
    ) {
        composable(Routes.SUBJECTS_LIST_SCREEN) {
            SubjectsScreen(
                navController = navController,
                onNavigateToSubjectDetails = { subjectId ->
                    navController.navigate("${Routes.SUBJECT_DETAILS_SCREEN}/$subjectId")
                }
            )
        }

        composable(
            route = "${Routes.SUBJECT_DETAILS_SCREEN}/{subjectId}",
            arguments = listOf(
                navArgument("subjectId") { nullable = false }
            )
        ) {
            SubjectDetailsScreen(
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}