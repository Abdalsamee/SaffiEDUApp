package com.example.saffieduapp.navigation

object Routes {
    // جذور الجرافات
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_GRAPH = "main_graph"
    const val TEACHER_GRAPH = "teacher_graph"

    // شاشات auth
    const val SPLASH_SCREEN = "splash_screen"
    const val ONBOARDING_SCREEN = "onboarding_screen"
    const val LOGIN_SCREEN = "login_screen"
    const val RESET_PASSWORD_SCREEN = "reset_password_screen"
    const val SIGNUP_SCREEN = "signup_screen"


    // تبويبات الجراف الرئيسي (كما ضبطتها سابقًا)
    const val HOME_SCREEN = "home_graph"
    const val SUBJECTS_SCREEN = "subjects_graph"

    //للمهام "طالب"
    const val TASKS_NAV_GRAPH = "tasks_nav_graph"
    const val TASKS_SCREEN = "tasks_screen"
    const val EXAM_DETAILS_SCREEN = "exam_details_screen"
    const val EXAM_SCREEN = "exam_screen"
    const val ASSIGNMENT_DETAILS_SCREEN = "assignment_details_screen"
    const val SUBMIT_ASSIGNMENT_SCREEN = "submit_assignment_screen"
    const val STUDENT_ASSIGNMENT_RESULT_SCREEN = "student_assignment_result_screen"
    const val STUDENT_EXAM_RESULT_SCREEN = "student_exam_result_screen"


    const val CHAT_SCREEN = "chat_graph"
    const val CHAT_DETAILS_SCREEN = "chat_details_screen"
    const val CHAT_DETAILS_ROUTE = "$CHAT_DETAILS_SCREEN/{chatId}/{senderName}"
    const val PROFILE_SCREEN = "profile_graph"

    // Children للمواد
    const val SUBJECTS_LIST_SCREEN = "subjects_list_screen"
    const val SUBJECT_DETAILS_SCREEN = "subject_details_screen"
    const val VIDEO_PLAYER_SCREEN = "video_player_screen"

    //Teacher  للمعلم
    const val TEACHER_MAIN_SCREEN = "teacher_main_screen"
    const val TEACHER_HOME_SCREEN = "teacher_home_screen"
    const val TEACHER_CLASSES_SCREEN = "teacher_classes_screen"
    const val TEACHER_PROFILE_SCREEN = "teacher_profile_screen"


    const val TEACHER_ADD_LESSON_SCREEN = "teacher_add_lesson_screen"
    const val TEACHER_ADD_ALERT_SCREEN = "teacher_add_alert_screen"
    const val TEACHER_ADD_ASSIGNMENT_SCREEN = "teacher_add_assignment_screen"

    //CREAT EXAM
    const val CREATE_QUIZ_GRAPH = "create_quiz_graph"
    const val ADD_EXAM_SCREEN = "add_exam_screen"
    const val ADD_QUESTION_SCREEN = "add_question_screen"
    const val QUIZ_SUMMARY_SCREEN = "quiz_summary_screen"

    //Teacher Tasks
    const val TEACHER_TASKS_GRAPH = "teacher_tasks_graph"
    const val TEACHER_TASKS_SCREEN = "teacher_tasks_screen"
    const val TEACHER_TASK_DETAILS_SCREEN = "teacher_task_details_screen"
    const val TEACHER_STUDENT_ASSIGNMENT_SCREEN = "teacher_student_assignment_screen"
    const val TEACHER_STUDENT_EXAM_SCREEN = "teacher_student_exam_screen"
    const val TEACHER_STUDENT_EXAM_ROUTE = "$TEACHER_STUDENT_EXAM_SCREEN/{studentId}"
    const val TEACHER_STUDENT_EXAM_ANSWERS_SCREEN_WITH_ARGS =
        "teacher_student_exam_answers_screen/{studentId}"

}
