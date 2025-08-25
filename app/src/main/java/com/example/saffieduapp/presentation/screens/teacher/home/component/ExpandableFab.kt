package com.example.saffieduapp.presentation.screens.teacher.home.component

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.saffieduapp.R
import com.example.saffieduapp.ui.theme.AppPrimary

@Composable
fun ExpandableFab(
    isExpanded: Boolean, // يستقبل الحالة من الخارج
    onFabClick: () -> Unit, // يخبر الخارج عند النقر عليه
    actions: List<FabActionItem>,
    onActionClick: (FabActionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // قائمة البطاقات المتحركة
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                actions.forEach { action ->
                    ActionItemCard(item = action, onClick = { onActionClick(action) })
                }
            }
        }

        // الزر الرئيسي
        FloatingActionButton(
            onClick = onFabClick, // استدعاء الدالة الخارجية
            modifier = Modifier.border(4.dp, Color.White, CircleShape).size(60.dp),
            shape = CircleShape,
            containerColor = AppPrimary,
            contentColor = Color.White
        ) {
            AnimatedContent(targetState = isExpanded, label = "fab_icon_animation") { expanded ->
                if (expanded) {
                    Icon(painter = painterResource(id = R.drawable.close), contentDescription = "Close")
                } else {
                    Icon(painterResource(id = R.drawable.plus), contentDescription = "Add")
                }
            }
        }
    }
}

// ... (ActionItemCard و FabActionItem يبقيان كما هما)
@Composable
private fun ActionItemCard(
    item: FabActionItem,
    onClick: () -> Unit
) {
    // هذا الكود يبقى كما هو بدون تغيير
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(width = 220.dp, height = 60.dp)
            .shadow(
                elevation = 22.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false,
                ambientColor = Color(0xFFCCCCCC),
                spotColor = Color(0xFFCCCCCC)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = R.drawable.addbtnn),
                contentDescription = "Add Action",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = item.label,
                color = AppPrimary
            )
            Icon(
                painter = item.icon,
                contentDescription = item.label,
                tint = AppPrimary,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

data class FabActionItem(
    val icon: Painter,
    val label: String
)