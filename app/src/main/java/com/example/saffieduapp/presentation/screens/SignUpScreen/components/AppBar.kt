package com.example.saffieduapp.presentation.screens.SignUpScreen.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SineUpAppBar(
    onBackClick: () -> Unit,
    screenWidth: Dp
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = "رجوع",
                        tint = Color.White // ✅ أيقونة بيضاء
                    )
                }
                Text(
                    text = "الاشتراك",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = (screenWidth.value * 0.08).sp, // ✅ نسبة من عرض الشاشة (~32sp)
                        fontWeight = FontWeight.W500,
                        color = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = screenWidth * 0.08f),
                    textAlign = TextAlign.Center
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppPrimary
        )
    )
}
