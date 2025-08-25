package com.example.saffieduapp.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.saffieduapp.navigation.BottomNavItem
import com.example.saffieduapp.navigation.Routes

import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.Cairo

// خريطة: مسار الجراف (التبّ) -> مسار شاشة البداية داخل الجراف
private val tabStartRoutes = mapOf(
    Routes.HOME_SCREEN to Routes.HOME_SCREEN,                    // لا جراف فرعي
    Routes.SUBJECTS_SCREEN to Routes.SUBJECTS_LIST_SCREEN,       // الجراف -> شاشة القائمة
    Routes.TASKS_SCREEN to Routes.TASKS_SCREEN,
    Routes.CHAT_SCREEN to Routes.CHAT_SCREEN,
    Routes.PROFILE_SCREEN to Routes.PROFILE_SCREEN
)

@Composable
fun AppBottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val noSelect = Color(0xFF7C7C7C)

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        val graphRoute = item.route
                        val startRoute = tabStartRoutes[graphRoute] ?: graphRoute

                        // 1) ارجع لشاشة البداية داخل نفس الجراف
                        navController.popBackStack(startRoute, inclusive = false)

                        // 2) بثّ حدث إعادة الاختيار لشاشة البداية
                        runCatching { navController.getBackStackEntry(startRoute) }
                            .getOrNull()
                            ?.savedStateHandle
                            ?.set("tab_reselected_tick", System.currentTimeMillis())
                    } else {
                        // انتقال بين تبّات مختلفة (يبقي الانزلاق)
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    AdaptiveLabel(
                        longText = item.title,
                        shortText = if (item.title == "الملف الشخصي") "ملفي" else item.title
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppPrimary,
                    selectedTextColor = AppPrimary,
                    unselectedIconColor = noSelect,
                    unselectedTextColor = noSelect,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun AdaptiveLabel(longText: String, shortText: String) {
    var useShortText by remember(longText, shortText) { mutableStateOf(false) }
    val textToShow = if (useShortText) shortText else longText
    Text(
        text = textToShow,
        fontSize = 11.sp,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { r -> if (!useShortText && r.didOverflowWidth) useShortText = true },
        fontFamily = Cairo
    )
}
