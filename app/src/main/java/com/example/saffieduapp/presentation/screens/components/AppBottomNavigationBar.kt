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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.saffieduapp.navigation.BottomNavItem
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun AppBottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val noSelect = Color(0xFF7C7C7C)

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
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
                    // استخدام المكون الذكي الجديد
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

    var useShortText by remember { mutableStateOf(false) }


    val textToShow = if (useShortText) shortText else longText

    Text(
        text = textToShow,
        fontSize = 11.sp,
        style = MaterialTheme.typography.bodyLarge
        , softWrap = false,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                useShortText = true
            }
        }
    )
}