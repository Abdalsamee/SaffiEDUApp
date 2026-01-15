package com.example.saffieduapp.navigation


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.saffieduapp.presentation.screens.chat.ChatScreen
import com.example.saffieduapp.presentation.screens.chatDetalis.ChatDetailScreen
import com.example.saffieduapp.presentation.screens.student.home.HomeScreen
import com.example.saffieduapp.presentation.screens.student.profile.StudentProfileScreen

private val topLevelOrder = listOf(
    Routes.HOME_SCREEN,
    Routes.SUBJECTS_SCREEN,
    Routes.TASKS_NAV_GRAPH,
    Routes.CHAT_SCREEN,
    Routes.PROFILE_SCREEN
)

private fun topRouteOf(entry: NavBackStackEntry): String {
    val self = entry.destination.route.orEmpty()
    val parent = entry.destination.parent?.route
    return when {
        parent != null && parent in topLevelOrder -> parent
        self in topLevelOrder -> self
        else -> parent ?: self
    }
}

private fun isTopLevel(entry: NavBackStackEntry) = topRouteOf(entry) in topLevelOrder

private fun slideDir(from: NavBackStackEntry, to: NavBackStackEntry): Int {
    val fi = topLevelOrder.indexOf(topRouteOf(from))
    val ti = topLevelOrder.indexOf(topRouteOf(to))
    return if (fi != -1 && ti != -1) {
        when {
            ti > fi -> -1
            ti < fi -> +1
            else -> 0
        }
    } else 0
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onFullscreenChange: ((Boolean) -> Unit)? = null,
    onLogoutNavigate: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME_SCREEN,
        route = Routes.MAIN_GRAPH,
        modifier = modifier,
        enterTransition = {
            val d = slideDir(initialState, targetState)
            when {
                initialState.destination.route == targetState.destination.route -> EnterTransition.None
                isTopLevel(initialState) && isTopLevel(targetState) && d != 0 -> slideInHorizontally(
                    animationSpec = tween(300)
                ) { if (d > 0) +it else -it }

                else -> fadeIn(animationSpec = tween(220))
            }
        },
        exitTransition = {
            val d = slideDir(initialState, targetState)
            when {
                initialState.destination.route == targetState.destination.route -> ExitTransition.None
                isTopLevel(initialState) && isTopLevel(targetState) && d != 0 -> slideOutHorizontally(
                    animationSpec = tween(300)
                ) { if (d > 0) -it else +it }

                else -> fadeOut(animationSpec = tween(220))
            }
        }) {
        composable(Routes.HOME_SCREEN) {
            HomeScreen(
                navController = navController,
                onNavigateToSubjects = { navController.navigate(Routes.SUBJECTS_SCREEN) })
        }

        subjectsNavGraph(navController, onFullscreenChange)
        tasksNavGraph(navController)

        composable(Routes.CHAT_SCREEN) {
            ChatScreen(navController = navController)
        }
        composable(
            route = "${Routes.CHAT_DETAILS_SCREEN}/{senderName}", // إضافة المتغير للمسار
            arguments = listOf(
                navArgument("senderName") { type = NavType.StringType })
        ) { backStackEntry ->
            // استخراج الاسم الممرر من الـ Arguments
            val senderName = backStackEntry.arguments?.getString("senderName") ?: "مستخدم"

            ChatDetailScreen(
                navController = navController, senderName = senderName
            )
        }

        composable(Routes.PROFILE_SCREEN) {
            StudentProfileScreen(
                onLogoutNavigate = onLogoutNavigate
            )
        }
    }
}