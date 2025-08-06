package com.example.saffieduapp.navigation

import androidx.compose.ui.graphics.painter.Painter


data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: Painter
)