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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    navController: NavHostController,

    viewModel: SplashViewModel = hiltViewModel()
) {

    val startDestination by viewModel.startDestination.collectAsState()

    val scaleCircle = remember { Animatable(0.1f) }
    val scaleLogo = remember { Animatable(0f) }


    LaunchedEffect(Unit) {
        val circleAnimationDuration = 700
        scaleCircle.animateTo(
            targetValue = 55f,
            animationSpec = tween(durationMillis = circleAnimationDuration, easing = FastOutSlowInEasing)
        )

        delay(150)

        val logoAnimationDuration = 700
        scaleLogo.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = logoAnimationDuration, easing = FastOutSlowInEasing)
        )


        val destination = snapshotFlow { startDestination }
            .filterNotNull()
            .first()


        navController.navigate(destination) {

            popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
                .size(20.dp)
                .scale(scaleCircle.value)
                .clip(CircleShape)
                .background(AppPrimary)
        )

        if (scaleLogo.value > 0f) {
            Image(
                painter = painterResource(id = R.drawable.logo_saffi),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(width = 134.dp, height = 144.dp)
                    .scale(scaleLogo.value)
            )
        }
    }
}