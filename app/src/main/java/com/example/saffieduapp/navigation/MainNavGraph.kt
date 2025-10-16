package com.example.saffieduapp.navigation

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.saffieduapp.presentation.screens.chat.ChatScreen
import com.example.saffieduapp.presentation.screens.student.home.HomeScreen
import com.example.saffieduapp.presentation.screens.student.profile.StudentProfileScreen

// ترتيب التبويبات (من اليسار لليمين في الشريط السفلي)
private val topLevelOrder = listOf(
    Routes.HOME_SCREEN,
    Routes.SUBJECTS_SCREEN,
    Routes.TASKS_NAV_GRAPH,
    Routes.CHAT_SCREEN,
    Routes.PROFILE_SCREEN
)

// حدد المسار العلوي الحقيقي للوجهة الحالية
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

// اتجاه الانزلاق بين تبّين علويين: +1 لليمين، -1 لليسار، 0 إن غير معرّف
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

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onFullscreenChange: ((Boolean) -> Unit)? = null
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
                isTopLevel(initialState) && isTopLevel(targetState) && d != 0 ->
                    slideInHorizontally(animationSpec = tween(300)) { if (d > 0) +it else -it }
                else -> fadeIn(animationSpec = tween(220))
            }
        },
        exitTransition = {
            val d = slideDir(initialState, targetState)
            when {
                initialState.destination.route == targetState.destination.route -> ExitTransition.None
                isTopLevel(initialState) && isTopLevel(targetState) && d != 0 ->
                    slideOutHorizontally(animationSpec = tween(300)) { if (d > 0) -it else +it }
                else -> fadeOut(animationSpec = tween(220))
            }
        },
        // Pop داخل نفس التبّ: بلا حركة. Pop بين تبّين علويين: انزلاق بالاتجاه الصحيح.
        popEnterTransition = {
            val sameTop = topRouteOf(initialState) == topRouteOf(targetState)
            val d = slideDir(initialState, targetState)
            when {
                sameTop -> EnterTransition.None
                isTopLevel(initialState) && isTopLevel(targetState) && d != 0 ->
                    slideInHorizontally(animationSpec = tween(300)) { if (d > 0) +it else -it }
                else -> EnterTransition.None
            }
        },
        popExitTransition = {
            val sameTop = topRouteOf(initialState) == topRouteOf(targetState)
            val d = slideDir(initialState, targetState)
            when {
                sameTop -> ExitTransition.None
                isTopLevel(initialState) && isTopLevel(targetState) && d != 0 ->
                    slideOutHorizontally(animationSpec = tween(300)) { if (d > 0) -it else +it }
                else -> ExitTransition.None
            }
        }
    ) {
        composable(Routes.HOME_SCREEN) {
            HomeScreen(
                navController = navController,
                onNavigateToSubjects = { navController.navigate(Routes.SUBJECTS_SCREEN) }
            )
        }
        subjectsNavGraph(
            navController,
            onFullscreenChange = onFullscreenChange
        )
        tasksNavGraph(navController)
        composable(Routes.CHAT_SCREEN) {

            ChatScreen(navController = navController)
        }
        composable(Routes.PROFILE_SCREEN)
        { StudentProfileScreen() }
    }
}
