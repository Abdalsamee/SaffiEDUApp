package com.example.saffieduapp.presentation.screens.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.saffieduapp.R
import com.example.saffieduapp.navigation.BottomNavItem
import com.example.saffieduapp.navigation.Routes
import com.example.saffieduapp.navigation.TeacherNavGraph
import com.example.saffieduapp.presentation.components.AppBottomNavigationBar
import com.example.saffieduapp.presentation.screens.teacher.home.component.ExpandableFab
import com.example.saffieduapp.presentation.screens.teacher.home.component.FabActionItem

@Composable
fun TeacherMainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(
            "الرئيسية",
            Routes.TEACHER_HOME_SCREEN,
            painterResource(id = R.drawable.homenot)
        ),
        BottomNavItem(
            "الصفوف",
            Routes.TEACHER_CLASSES_SCREEN,
            painterResource(id = R.drawable.rclass)
        ),
        BottomNavItem(
            "المهام",
            Routes.TEACHER_TASKS_SCREEN,
            painterResource(id = R.drawable.tasks)
        ),
        BottomNavItem("الدردشة", Routes.TEACHER_CHAT_SCREEN, painterResource(id = R.drawable.chat)),
        BottomNavItem(
            "الملف الشخصي",
            Routes.TEACHER_PROFILE_SCREEN,
            painterResource(id = R.drawable.user)
        )
    )

    val fabActions = listOf(
        FabActionItem(
            icon = painterResource(id = R.drawable.books),
            label = "إضافة درس"
        ),
        FabActionItem(
            icon = painterResource(id = R.drawable.assignment),
            label = "إضافة واجب"
        ),
        FabActionItem(
            icon = painterResource(id = R.drawable.exam),
            label = "إضافة اختبار"
        ),
        FabActionItem(
            icon = painterResource(id = R.drawable.alert),
            label = "إضافة تنبيه"
        )
    )

    var isFabExpanded by remember { mutableStateOf(false) }

    // Enhanced animation for scrim
    val scrimAlpha by animateFloatAsState(
        targetValue = if (isFabExpanded) 0.1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "scrim_alpha"
    )

    // Function to handle FAB expansion toggle with better state management
    val toggleFab = { isFabExpanded = !isFabExpanded }

    // Function to close FAB with proper state reset
    val closeFab = {
        if (isFabExpanded) {
            isFabExpanded = false
        }
    }

    // Handle back gesture when FAB is expanded
    val view = LocalView.current
    LaunchedEffect(isFabExpanded) {
        if (isFabExpanded) {
            // Optional: Add haptic feedback when FAB expands
            // view.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Scaffold with content
        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(items = bottomNavItems, navController = navController)
            },
            // Remove FAB from Scaffold to handle it manually with proper layering
        ) { innerPadding ->
            TeacherNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // Scrim layer - appears above content but below FAB
        AnimatedVisibility(
            visible = isFabExpanded,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier.zIndex(1f)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = closeFab
                    )
                    .graphicsLayer {
                        alpha = scrimAlpha
                    },
                color = Color.Black
            ) {}
        }

        // FAB layer - highest z-index, positioned manually
        if (currentRoute == Routes.TEACHER_HOME_SCREEN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, bottom = 140.dp, end = 16.dp, top = 16.dp)
                    .zIndex(10f), // Very high z-index to ensure it's above everything
                contentAlignment = Alignment.BottomStart
            ) {
                ExpandableFab(
                    isExpanded = isFabExpanded,
                    onFabClick = toggleFab,
                    actions = fabActions,
                    onActionClick = { action ->
                        closeFab() // Close FAB first

                        // Handle navigation based on action
                        when (action.label) {
                            "إضافة درس" -> {
                                navController.navigate(Routes.TEACHER_ADD_LESSON_SCREEN)
                            }
                            "إضافة واجب" -> {
                                // Handle assignment creation
                                println("Clicked on: إضافة واجب")
                            }
                            "إضافة اختبار" -> {
                                // Handle exam creation
                                println("Clicked on: إضافة اختبار")
                            }
                            "إضافة تنبيه" -> {
                                // Handle alert creation
                                println("Clicked on: إضافة تنبيه")
                            }
                        }
                    }
                )
            }
        }
    }
}