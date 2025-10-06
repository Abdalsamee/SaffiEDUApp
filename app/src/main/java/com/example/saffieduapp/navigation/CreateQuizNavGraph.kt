package com.example.saffieduapp.navigation

import android.annotation.SuppressLint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamScreen
import com.example.saffieduapp.presentation.screens.teacher.add_question.AddQuestionScreen
import com.example.saffieduapp.presentation.screens.teacher.add_question.AddQuestionViewModel
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import com.example.saffieduapp.presentation.screens.teacher.quiz_summary.QuizSummaryScreen

@SuppressLint("StateFlowValueCalledInComposition")
fun NavGraphBuilder.createQuizNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.ADD_EXAM_SCREEN,
        route = Routes.CREATE_QUIZ_GRAPH
    ) {
        // الوجهة الأولى: إعدادات الاختبار
        composable(Routes.ADD_EXAM_SCREEN) {
            AddExamScreen(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToNext = {
                    // الانتقال إلى شاشة إضافة الأسئلة
                    navController.navigate(Routes.ADD_QUESTION_SCREEN)
                }
            )
        }

        // الوجهة الثانية: إضافة الأسئلة
        composable(Routes.ADD_QUESTION_SCREEN) {
            AddQuestionScreen(
                navController = navController, // ✅ أضف هذا
                onNavigateUp = { navController.popBackStack() },
                onNavigateToSummary = {
                    navController.navigate(Routes.QUIZ_SUMMARY_SCREEN)
                }
            )
    }

        // الوجهة الثالثة: ملخص الأسئلة
        composable(Routes.QUIZ_SUMMARY_SCREEN) { backStackEntry ->
            val questions = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<List<QuestionData>>("questions") ?: emptyList()

            QuizSummaryScreen(
                questions = questions,
                onNavigateUp = { navController.popBackStack() },
                onPublish = {
                    navController.popBackStack(Routes.CREATE_QUIZ_GRAPH, inclusive = true)
                }
            )
    }
    }
}