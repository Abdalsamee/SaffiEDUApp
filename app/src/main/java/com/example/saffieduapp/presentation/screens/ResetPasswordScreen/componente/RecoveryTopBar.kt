package com.example.saffieduapp.presentation.screens.ResetPasswordScreen.componente


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppBackground
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryTopBar(
    onBackClicked: () -> Unit,
    fontSize: TextUnit
) {
    TopAppBar(
        title = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "استعادة كلمة المرور",
                    style = Typography.titleLarge.copy(fontSize = fontSize),
                    color = AppTextPrimary
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    contentDescription = "رجوع",
                    tint = AppTextPrimary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppBackground
        )
    )
}
