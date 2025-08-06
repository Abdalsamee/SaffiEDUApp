package com.example.saffieduapp.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // ✅ تحويل القيم إلى نسب
    val horizontalPadding = screenWidth * 0.10f   // 85dp ≈ 23% من العرض
    val buttonWidth = screenWidth - (horizontalPadding * 1)
    val buttonHeight = screenHeight * 0.07f       // 58dp ≈ 7% من الارتفاع
    val cornerRadius = screenWidth * 0.06f        // 20dp ≈ 5% من العرض

    Button(
        onClick = onClick,
        modifier = modifier
            .padding(start = horizontalPadding, end = horizontalPadding)
            .width(buttonWidth)
            .height(buttonHeight),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.W500,
            color = Color.White
        )
    }
}
