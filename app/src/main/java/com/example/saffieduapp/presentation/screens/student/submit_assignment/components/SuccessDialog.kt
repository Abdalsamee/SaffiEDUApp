package com.example.saffieduapp.presentation.screens.student.submit_assignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.saffieduapp.R

@Composable
fun SuccessDialog(
    onDismiss: () -> Unit,
    submitDate: Long? = null
) {
    // تنسيق التاريخ والوقت
    val formattedTime = submitDate?.let {
        java.text.SimpleDateFormat(
            "dd MMMM yyyy، الساعة HH:mm:ss",
            java.util.Locale("ar")
        ).format(java.util.Date(it))
    } ?: "غير محدد"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تم تسليم الواجب بنجاح!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "تاريخ التسليم:\n$formattedTime",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = null,
                    tint = Color(0xFF27AE60),
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("العودة إلى المهام", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SuccessDialogPreview() {
    MaterialTheme {
        SuccessDialog(
            onDismiss = {},
            submitDate = 0
        )
    }
}
