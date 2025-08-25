package com.example.saffieduapp.presentation.screens.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
        FabActionItem(icon = painterResource(id = R.drawable.books), label = "إضافة درس"),
        FabActionItem(icon = painterResource(id = R.drawable.assignment), label = "إضافة واجب"),
        FabActionItem(icon = painterResource(id = R.drawable.exam), label = "إضافة اختبار"),
        FabActionItem(icon = painterResource(id = R.drawable.alert), label = "إضافة تنبيه")
    )

    var isFabExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            bottomBar = {
                AppBottomNavigationBar(items = bottomNavItems, navController = navController)
            }
        ) { innerPadding ->
            TeacherNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        // خلفية شفافة تغلق القائمة عند الضغط (تحت القائمة مباشرة)
        if (isFabExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f) // خلفية أقل مستوى
                    .noRippleClickable { isFabExpanded = false },
                color = Color.Black.copy(alpha = 0.1f)
            ) {}
        }

        // الزر العائم وقائمته (فوق الخلفية)
        ExpandableFab(
            isExpanded = isFabExpanded,
            onFabClick = { isFabExpanded = !isFabExpanded },
            actions = fabActions,
            onActionClick = { action ->
                println("Clicked on: ${action.label}")
                isFabExpanded = false
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 100.dp, start = 16.dp)
                .zIndex(1f)
        )
    }
}




fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier {
    return this.clickable(
        indication = null,
        interactionSource = MutableInteractionSource(),
        onClick = onClick
    )
}
