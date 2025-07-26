package com.example.saffieduapp.presentation.screens.onboarding.components

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.ui.theme.AppPrimary

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun CustomCurvedShapeBox(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .padding(horizontal = 5.dp)
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val density = LocalDensity.current
        val strokeWidth = with(density) { 20.dp.toPx() }
        val cornerRadius = with(density) { 18.dp.toPx() }
        val leftY = height * 0.82f
        val rightY = height * 0.67f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(strokeWidth / 2, leftY)
                lineTo(strokeWidth / 2, strokeWidth / 2 + cornerRadius)

                arcTo(
                    rect = Rect(
                        left = strokeWidth / 2,
                        top = strokeWidth / 2,
                        right = strokeWidth / 2 + cornerRadius * 2,
                        bottom = strokeWidth / 2 + cornerRadius * 2
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(width - cornerRadius - strokeWidth / 2, strokeWidth / 2)

                arcTo(
                    rect = Rect(
                        left = width - cornerRadius * 2 - strokeWidth / 2,
                        top = strokeWidth / 2,
                        right = width - strokeWidth / 2,
                        bottom = strokeWidth / 2 + cornerRadius * 2
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )

                lineTo(width - strokeWidth / 2, rightY)
            }

            drawPath(
                path = path,
                color = AppPrimary,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}

@Composable
fun CurvedImageOnly(
    @DrawableRes imageRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.94f)
            .aspectRatio(1.15f)
            .padding(top = 23.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
