package com.example.saffieduapp.presentation.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val startDestination by viewModel.startDestination.collectAsState()
    val scaleCircle = remember { Animatable(0.1f) }
    val scaleLogo = remember { Animatable(0f) }

    // رسوم الحركة
    LaunchedEffect(Unit) {
        scaleCircle.animateTo(
            targetValue = 55f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
        delay(150)
        scaleLogo.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
        )
    }

    // الانتقال عند توفر الوجهة
    LaunchedEffect(startDestination) {
        startDestination?.let { destination ->
            onNavigate(destination)
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
