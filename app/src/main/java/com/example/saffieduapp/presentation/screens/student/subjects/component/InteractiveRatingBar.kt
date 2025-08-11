package com.example.saffieduapp.presentation.screens.student.subjects.component


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.R

@Composable
fun InteractiveRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 24.dp,
    spacing: Dp = 4.dp
) {
    Row(modifier = modifier,horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(5) { index ->
            val starNumber = index + 1
            Icon(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "Star $starNumber",
                tint = if (starNumber <= rating) Color(0xFFFFC107) else Color(0xFFDAD5C5),
                modifier = Modifier.
                    size(starSize)
                    .clickable { onRatingChanged(starNumber) }
            )

        }
    }
}