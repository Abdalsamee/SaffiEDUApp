package com.example.saffieduapp.presentation.screens.Chats.ChatList.Component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.R

@Composable
fun CustomSearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color(0x8AE4E4E4), RoundedCornerShape(12.dp))
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.CenterStart // تم التعديل هنا
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // تم التعديل هنا
        ) {
            Image(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "Search Icon",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "البحث",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start, // تم التعديل هنا
                modifier = Modifier.padding(start = 8.dp) // تم التعديل هنا
            )
        }
    }
}

@Preview(showBackground = true, locale = "ar") // تمت إضافة `locale = "ar"`
@Composable
fun SearchBarPreview() {
    CustomSearchBar()
}