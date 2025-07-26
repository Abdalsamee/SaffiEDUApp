package com.example.saffieduapp.presentation.screens.unboarding.model

import com.example.saffieduapp.R

data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)
val onboardingPages = listOf(
    OnboardingPageData(
        R.drawable.unboarding1,
        "تعلّم بلا حدود",
        "استكشف علماً من الجودة المصممة خصيصاً لك! اكتشف محتوى تعليمي شيّق في أي وقت ومكان."
    ),
    OnboardingPageData(
        R.drawable.unboarding2,
        "محتوى متنوع يناسب الجميع",
        "من البرامج إلى الرياضيات والعلوم، محتوى شامل للجميع مع أدوات واستراتيجيات تساعدك على النجاح."
    ),
    OnboardingPageData(
        R.drawable.unboarding3,
        "تعلّم وحقق أهدافك",
        "تابع تقدمك، أنشئ أهدافك، وابدأ طريقك نحو مستقبل أفضل."
    )
)
