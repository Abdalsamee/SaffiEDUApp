package com.example.saffieduapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.student.subject_details.SubjectDetailsScreen
import com.example.saffieduapp.presentation.screens.student.subjects.SubjectsScreen
import com.example.saffieduapp.presentation.screens.student.video_player.VideoPlayerScreen

fun NavGraphBuilder.subjectsNavGraph(navController: NavController,
                                     onFullscreenChange: ((Boolean) -> Unit)? = null) {
    navigation(
        startDestination = Routes.SUBJECTS_LIST_SCREEN,
        route = Routes.SUBJECTS_SCREEN
    ) {
        // ١. شاشة قائمة المواد
        composable(Routes.SUBJECTS_LIST_SCREEN) {
            SubjectsScreen(
                onNavigateToSubjectDetails = { subjectId ->
                    navController.navigate("${Routes.SUBJECT_DETAILS_SCREEN}/$subjectId")
                },
                onNavigateUp = {
                    navController.popBackStack()
                }
            )
        }

        // ٢. شاشة تفاصيل المادة
        composable(
            route = "${Routes.SUBJECT_DETAILS_SCREEN}/{subjectId}",
            arguments = listOf(
                navArgument("subjectId") { type = NavType.StringType }
            )
        ) {
            SubjectDetailsScreen(
                onNavigateUp = { navController.popBackStack() },
                navController = navController // <-- الإضافة المطلوبة هنا
            )
        }

        // ٣. شاشة الفيديو
        composable(
            route = "${Routes.VIDEO_PLAYER_SCREEN}/{videoId}",
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) {
            VideoPlayerScreen(
                onNavigateUp = { navController.popBackStack() },
                onFullscreenChange = onFullscreenChange
            )
        }
    }
}