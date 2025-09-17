package com.example.saffieduapp.presentation.screens


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.navigation.BottomNavItem
import com.example.saffieduapp.presentation.components.AppBottomNavigationBar
import com.example.saffieduapp.R
import com.example.saffieduapp.navigation.MainNavGraph
import com.example.saffieduapp.navigation.Routes

@Composable
fun MainAppScreen() {
    val navController: NavHostController = rememberNavController()
    var isVideoFullscreen by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        BottomNavItem(
            title = "الرئيسية",
            route = Routes.HOME_SCREEN,
            icon = painterResource(id = R.drawable.homenot),

            ),
        BottomNavItem(
            title = "المواد",
            route = Routes.SUBJECTS_SCREEN,
            icon = painterResource(id = R.drawable.subject),

            ),
        BottomNavItem(
            title = "المهام",
            route = Routes.TASKS_SCREEN,
            icon = painterResource(id = R.drawable.tasks),

            ),
        BottomNavItem(
            title = "الدردشة",
            route = Routes.CHAT_SCREEN,
            icon = painterResource(id = R.drawable.chat),

            ),
        BottomNavItem(
            title = "الملف الشخصي",
            route = Routes.PROFILE_SCREEN,
            icon = painterResource(id = R.drawable.user),

            )
    )

    Scaffold(
        bottomBar = {
            // إخفاء الـ BottomBar عند fullscreen
            if (!isVideoFullscreen) {
                AppBottomNavigationBar(items = bottomNavItems, navController = navController)
            }
        }
    ) { innerPadding ->

        MainNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onFullscreenChange = { fullscreen ->
                isVideoFullscreen = fullscreen
            }
        )
    }
}