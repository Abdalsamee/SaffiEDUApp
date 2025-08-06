package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable

fun HomeTopSection(
    studentName: String,
    studentGrade: String,
    profileImageUrl: String,

) {



    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            .background(AppPrimary),
                contentAlignment = Alignment.Center
    ) {

        // Row to hold the content inside the header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(width = 1.dp,
                        color = AppPrimary,
                        shape = RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.rectangle),
                error = androidx.compose.ui.res.painterResource(id = R.drawable.rectangle)
            )
            Column(horizontalAlignment = Alignment.Start) {
                Text(text = "مرحباً 👋", color = Color.White, fontSize = 16.sp ,fontWeight = FontWeight.Bold)
                Text(
                    text = "$studentName ($studentGrade)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }


        }
    }
}