package com.example.saffieduapp.presentation.screens.student.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.saffieduapp.R

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(55.dp), // تحديد الارتفاع
        placeholder = { Text(text = "البحث",
            fontSize = 15.sp,

            ) },
        leadingIcon = {
            // استخدام AsyncImage لعرض أيقونة SVG
            AsyncImage(
                model = R.drawable.search, // استبدل باسم أيقونتك
                contentDescription = "Search Icon",
                modifier = Modifier.size(30.dp)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp), // تحديد الـ radius
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedContainerColor = Color(0xFFE4E4E4), // اللون الذي حددته
            focusedContainerColor = Color(0xFFE4E4E4)
        )
    )
}