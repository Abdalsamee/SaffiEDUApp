package com.example.saffieduapp.presentation.screens.onboarding

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.onboarding.components.CustomCurvedShapeBox
import com.example.saffieduapp.presentation.screens.onboarding.components.CurvedImageOnly
import com.example.saffieduapp.presentation.screens.unboarding.OnboardingViewModel
import com.example.saffieduapp.presentation.screens.unboarding.model.onboardingPages
import com.example.saffieduapp.ui.theme.AppPrimary
import com.example.saffieduapp.ui.theme.AppTextPrimary
import com.example.saffieduapp.ui.theme.Typography
import kotlinx.coroutines.launch

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    // 1. تم تغيير الـ NavController إلى دالة lambda
    onNavigateToLogin: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState { onboardingPages.size }
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == onboardingPages.lastIndex

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    if (pagerState.currentPage > 0) {
                        IconButton(onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_left),
                                contentDescription = "رجوع",
                                tint = Color.Black
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // 2. تم تحديث أمر الانتقال
                        viewModel.onFinishClick()
                        onNavigateToLogin()
                    }) {
                        Text(text = "تخطي", color = AppTextPrimary, fontSize = 15.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ... (The rest of your UI code remains exactly the same)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.15f),
                contentAlignment = Alignment.TopCenter
            ) {
                CustomCurvedShapeBox()
                CurvedImageOnly(imageRes = onboardingPages[pagerState.currentPage].imageRes)
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 24.dp)
            ) { pageIndex ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = onboardingPages[pageIndex].title,
                        style = Typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = onboardingPages[pageIndex].description,
                        style = Typography.bodyMedium,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    repeat(onboardingPages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val animatedWidth by animateDpAsState(
                            targetValue = if (isSelected) 30.dp else 9.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "indicatorWidthAnimation"
                        )
                        val animatedHeight by animateDpAsState(
                            targetValue = 9.dp,
                            animationSpec = tween(durationMillis = 300),
                            label = "indicatorHeightAnimation"
                        )
                        val color = if (isSelected) AppPrimary else Color.LightGray
                        val shape = if (isSelected) RoundedCornerShape(9.dp) else CircleShape

                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .width(animatedWidth)
                                .height(animatedHeight)
                                .background(color = color, shape = shape)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(20.dp))
                Button(
                    onClick = {
                        if (isLastPage) {
                            // 3. تم تحديث أمر الانتقال هنا أيضاً
                            viewModel.onFinishClick()
                            onNavigateToLogin()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppPrimary)
                ) {
                    Text(
                        text = if (isLastPage) "ابدأ" else "التالي",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}