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
                navController = navController
            )
        }

        // ٣. شاشة الفيديو (Base64 كـ query parameter مشفر URI)
        composable(
            route = "${Routes.VIDEO_PLAYER_SCREEN}?videoBase64={videoBase64}",
            arguments = listOf(
                navArgument("videoBase64") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // فك ترميز URI للوصول للـ Base64 الأصلي
            val encodedBase64 = backStackEntry.arguments?.getString("videoBase64")
            val videoBase64 = encodedBase64?.let { Uri.decode(it) }

            VideoPlayerScreen(
                base64String = videoBase64,
                onNavigateUp = { navController.popBackStack() },
                onFullscreenChange = onFullscreenChange,
                navController = navController // ✅ تمرير navController هنا
            )
        }
    }
}

/**
 * دالة مساعدة للتنقل إلى شاشة الفيديو
 * تقوم بترميز Base64 قبل الإرسال لتجنب مشاكل URL الطويلة
 */
fun NavController.navigateToVideoScreen(base64String: String) {
    val encodedBase64 = Uri.encode(base64String)
    this.navigate("${Routes.VIDEO_PLAYER_SCREEN}?videoBase64=$encodedBase64")
}
