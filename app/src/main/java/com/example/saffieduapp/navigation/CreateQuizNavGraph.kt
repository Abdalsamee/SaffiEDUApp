package com.example.saffieduapp.navigation

import android.annotation.SuppressLint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamScreen
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamState
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
                onNavigateToNext = { examState ->
                    // نحفظ examState في BackStackEntry الحالي
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "examState",
                        examState
                    )
                    navController.navigate(Routes.ADD_QUESTION_SCREEN)
                }
            )
        }

        // الوجهة الثانية: إضافة الأسئلة
        composable(Routes.ADD_QUESTION_SCREEN) {
            AddQuestionScreen(
                navController = navController, // ✅ أضف هذا
                onNavigateUp = { navController.popBackStack() },
                onNavigateToSummary = { questions ->
                    // احفظ الأسئلة في SavedStateHandle للـ QuizSummaryScreen
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "questions",
                        questions
                    )
                    navController.navigate(Routes.QUIZ_SUMMARY_SCREEN)
                }
            )
        }

        // الوجهة الثالثة: ملخص الأسئلة
        // الوجهة الثالثة: ملخص الأسئلة
        composable(Routes.QUIZ_SUMMARY_SCREEN) { backStackEntry ->
            // جلب examState من شاشة AddExamScreen
            val examState = navController.getBackStackEntry(Routes.ADD_EXAM_SCREEN)
                .savedStateHandle
                .get<AddExamState>("examState") ?: AddExamState()

            // جلب الأسئلة من شاشة AddQuestionScreen
            val questions = navController.getBackStackEntry(Routes.ADD_QUESTION_SCREEN)
                .savedStateHandle
                .get<List<QuestionData>>("questions") ?: emptyList()

            QuizSummaryScreen(
                examState = examState,
                questions = questions,
                onNavigateUp = { navController.popBackStack() },
                onPublish = {
                    navController.popBackStack(Routes.CREATE_QUIZ_GRAPH, inclusive = true)
                }
            )
        }
    }
}