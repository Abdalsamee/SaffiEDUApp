package com.example.saffieduapp.presentation.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    var showWhiteCircle by remember { mutableStateOf(false) }

    val scale = remember { Animatable(0.1f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 55f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
        delay(200)
        showWhiteCircle = true
        delay(11500)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(20.dp) // 👈 أكبر قليلاً من 20dp
                .scale(scale.value)
                .clip(CircleShape)
                .background(AppPrimary)
        )

        if (showWhiteCircle) {
            Box(
                modifier = Modifier
                    .size(227.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_saffi),
                    contentDescription = "Logo",
                    modifier = Modifier.size(width = 134.dp , height = 144.dp)
                )
            }
        }
    }
}
