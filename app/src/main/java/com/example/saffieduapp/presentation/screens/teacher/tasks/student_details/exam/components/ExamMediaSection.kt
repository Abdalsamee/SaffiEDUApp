package com.example.saffieduapp.presentation.screens.teacher.tasks.student_details.exam.components

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.saffieduapp.ui.theme.AppTextSecondary
import com.example.saffieduapp.ui.theme.SaffiEDUAppTheme

@Composable
fun ExamMediaSection(
    imageUrls: List<String>,
    videoUrl: String?,
    onImageClick: (String) -> Unit = {},
    onVideoClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // 🔹 القسم الأول: الصور
        Text(
            text = "لقطات المراقبة (صور):",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppTextSecondary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(imageUrls) { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "صورة مراقبة",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFDDE9FF))
                        .clickable { onImageClick(url) },
                    alignment = Alignment.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 القسم الثاني: الفيديو
        Text(
            text = "لقطات المراقبة (فيديو):",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = AppTextSecondary
        )

        if (!videoUrl.isNullOrEmpty()) {
            val exoPlayer = ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = false
            }

            AndroidView(
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                        useController = true
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            220.dp.value.toInt()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDDE9FF))
                    .clickable { onVideoClick?.invoke() }
            )
        } else {
            Text(
                text = "لا يوجد فيديو متاح.",
                fontSize = 14.sp,
                color = AppTextSecondary
            )
        }
    }
}
