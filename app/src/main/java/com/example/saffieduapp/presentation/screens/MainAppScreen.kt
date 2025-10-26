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
fun MainAppScreen(
    onLogoutNavigate: () -> Unit
) {
    val navController = rememberNavController()
    var isVideoFullscreen by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        BottomNavItem("الرئيسية", Routes.HOME_SCREEN, painterResource(R.drawable.homenot)),
        BottomNavItem("المواد", Routes.SUBJECTS_SCREEN, painterResource(R.drawable.subject)),
        BottomNavItem("المهام", Routes.TASKS_NAV_GRAPH, painterResource(R.drawable.tasks)),
        BottomNavItem("الدردشة", Routes.CHAT_SCREEN, painterResource(R.drawable.chat)),
        BottomNavItem("الملف الشخصي", Routes.PROFILE_SCREEN, painterResource(R.drawable.user))
    )

    Scaffold(
        bottomBar = {
            if (!isVideoFullscreen) {
                AppBottomNavigationBar(items = bottomNavItems, navController = navController)
            }
        }) { innerPadding ->
        MainNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            onFullscreenChange = { fullscreen -> isVideoFullscreen = fullscreen },
            onLogoutNavigate = onLogoutNavigate
        )
    }
}
