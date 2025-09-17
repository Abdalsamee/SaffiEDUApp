package com.example.saffieduapp.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.student.subject_details.SubjectDetailsScreen
import com.example.saffieduapp.presentation.screens.student.subjects.SubjectsScreen
import com.example.saffieduapp.presentation.screens.student.video_player.VideoPlayerScreen

fun NavGraphBuilder.subjectsNavGraph(
    navController: NavController,
    onFullscreenChange: ((Boolean) -> Unit)? = null
) {
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
                onNavigateUp = { navController.popBackStack() }
            )
        }

        // ٢. شاشة تفاصيل المادة
        composable(
            route = Routes.SUBJECT_DETAILS_SCREEN + "/{subjectId}"
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""

            SubjectDetailsScreen(
                onNavigateUp = { navController.popBackStack() },
                navController = navController,
                subjectId = subjectId   // مرر الـ id
            )
        }

        composable(
            route = "${Routes.VIDEO_PLAYER_SCREEN}?videoUrl={videoUrl}",
            arguments = listOf(
                navArgument("videoUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val videoUrl = backStackEntry.arguments?.getString("videoUrl")?.let { Uri.decode(it) }

            // تمرير videoUrl إلى ViewModel عبر SavedStateHandle
            videoUrl?.let { url ->
                backStackEntry.savedStateHandle["videoUrl"] = url
            }

            VideoPlayerScreen(
                navController = navController,
                onNavigateUp = { navController.popBackStack() },
                onFullscreenChange = onFullscreenChange
            )
        }
    }
}

/**
 * دالة مساعدة للتنقل إلى شاشة الفيديو
 * تمرير Download URL مباشرة
 */
fun NavController.navigateToVideoScreen(videoUrl: String) {
    val encodedUrl = Uri.encode(videoUrl)
    this.navigate("${Routes.VIDEO_PLAYER_SCREEN}?videoUrl=$encodedUrl")
}
