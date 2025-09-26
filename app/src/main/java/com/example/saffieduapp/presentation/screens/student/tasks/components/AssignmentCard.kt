package com.example.saffieduapp.presentation.screens.student.tasks.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.student.tasks.AssignmentItem
import com.example.saffieduapp.presentation.screens.student.tasks.AssignmentStatus
import com.example.saffieduapp.ui.theme.AppAccent
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun AssignmentCard(
    assignment: AssignmentItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Determine button text and color based on assignment status
    val buttonText: String
    val buttonColor: Color
    val isButtonEnabled: Boolean

    when (assignment.status) {
        AssignmentStatus.SUBMITTED -> {
            buttonText = "عرض التسليم"
            buttonColor = AppAccent// Green
            isButtonEnabled = true
        }
        AssignmentStatus.PENDING -> {
            buttonText = "تسليم"
            buttonColor = AppPrimary // Blue
            isButtonEnabled = true
        }
        AssignmentStatus.LATE -> {
            buttonText = "متأخر"
            buttonColor = Color(0xFFF2994A) // Orange
            isButtonEnabled = true
        }
        // --- The missing case has been added here ---
        AssignmentStatus.EXPIRED -> {
            buttonText = "انتهى"
            buttonColor = Color(0xFFF7C292)
            isButtonEnabled = true
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black,
                clip = false),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,      // خلفية بيضاء
            contentColor = Color.Black         // لون النص داخل البطاقة
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Top section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.defultsubject), // assignment.imageUrl
                    contentDescription = assignment.title,
                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = assignment.title, fontWeight = FontWeight.Bold)
                    Text(text = assignment.subjectName, color = Color.Gray, fontSize = 14.sp)
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            // Middle section
            Text(text = assignment.dueDate, fontSize = 12.sp)
            Text(text = assignment.remainingTime, fontSize = 12.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom section
            Button(
                onClick = onClick,
                enabled = isButtonEnabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
            ) {
                Text(text = buttonText, color = Color.White)
            }
        }
    }
}