package com.example.saffieduapp.navigation

object Routes {
    // جذور الجرافات
    const val AUTH_GRAPH  = "auth_graph"
    const val MAIN_GRAPH  = "main_graph"

    // شاشات auth
    const val SPLASH_SCREEN     = "splash_screen"
    const val ONBOARDING_SCREEN = "onboarding_screen"
    const val LOGIN_SCREEN      = "login_screen"
    const val SIGNUP_SCREEN     = "signup_screen"

    // تبويبات الجراف الرئيسي (كما ضبطتها سابقًا)
    const val HOME_SCREEN      = "home_graph"
    const val SUBJECTS_SCREEN  = "subjects_graph"
    const val TASKS_SCREEN     = "tasks_graph"
    const val CHAT_SCREEN      = "chat_graph"
    const val PROFILE_SCREEN   = "profile_graph"

    // Children للمواد
    const val SUBJECTS_LIST_SCREEN    = "subjects_list_screen"
    const val SUBJECT_DETAILS_SCREEN  = "subject_details_screen"
}
