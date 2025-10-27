package com.example.saffieduapp.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamScreen
import com.example.saffieduapp.presentation.screens.teacher.add_exam.AddExamState
import com.example.saffieduapp.presentation.screens.teacher.add_question.AddQuestionScreen
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionData
import com.example.saffieduapp.presentation.screens.teacher.quiz_summary.QuizSummaryScreen

@SuppressLint("StateFlowValueCalledInComposition")
fun NavGraphBuilder.createQuizNavGraph(navController: NavController) {
    navigation(
        startDestination = Routes.ADD_EXAM_SCREEN, route = Routes.CREATE_QUIZ_GRAPH
    ) {
        // الوجهة الأولى: إعدادات الاختبار
        composable(Routes.ADD_EXAM_SCREEN) {
            AddExamScreen(
                onNavigateUp = { navController.popBackStack() },
                onNavigateToNext = { examState ->
                    // نحفظ examState في BackStackEntry الحالي
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "examState", examState
                    )
                    navController.navigate(Routes.ADD_QUESTION_SCREEN)
                })
        }

        // الوجهة الثانية: إضافة الأسئلة
        composable(Routes.ADD_QUESTION_SCREEN) { backStackEntry ->

            // 💡 ملاحظة: تمت إزالة استدعاء ViewModel هنا

            val questionToEdit =
                navController.previousBackStackEntry?.savedStateHandle?.get<QuestionData>("questionToEdit")
            val allQuestions =
                navController.previousBackStackEntry?.savedStateHandle?.get<List<QuestionData>>("allQuestions")
                    ?: emptyList()

            // ✅ يجب إزالة المفتاح بعد استخدامه لتجنب التعديل مرة أخرى إذا انتقلنا لـ ADD_QUESTION_SCREEN بطريقة عادية
            navController.previousBackStackEntry?.savedStateHandle?.remove<QuestionData>("questionToEdit")
            navController.previousBackStackEntry?.savedStateHandle?.remove<List<QuestionData>>("allQuestions") // إزالة القائمة أيضاً

            AddQuestionScreen(
                navController = navController,
                questionToEdit = questionToEdit, // تمرير السؤال إلى الشاشة للتعديل
                allQuestions = allQuestions, // <--- تمرير القائمة المسترجعة
                onNavigateUp = { navController.popBackStack() },
                onNavigateToSummary = { questions ->
                    // 🎯 التصحيح: احفظ الأسئلة في الـ SavedStateHandle الخاص بالوجهة الحالية
                    // (ADD_QUESTION_SCREEN) ليتم قراءتها منها في الملخص.
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "questions", questions
                    )
                    navController.navigate(Routes.QUIZ_SUMMARY_SCREEN)
                })
        }

        // الوجهة الثالثة: ملخص الأسئلة
        composable(Routes.QUIZ_SUMMARY_SCREEN) { backStackEntry ->
            // 1. استخدام remember لحفظ حالة الامتحان (لأنها ثابتة)
            val examState = remember {
                navController.getBackStackEntry(Routes.ADD_EXAM_SCREEN).savedStateHandle.get<AddExamState>(
                    "examState"
                ) ?: AddExamState()
            }

            // 2. 💡 الحل: جلب قائمة الأسئلة مباشرة من SavedStateHandle
            //     الخاص بـ ADD_QUESTION_SCREEN. هذا يضمن الحصول على القائمة المحدثة.
            val questions =
                navController.getBackStackEntry(Routes.ADD_QUESTION_SCREEN).savedStateHandle.get<List<QuestionData>>(
                    "questions"
                ) ?: emptyList()


            // 3. قم بتمرير examState و questions إلى الشاشة
            QuizSummaryScreen(
                examState = examState,
                questions = questions, // ✅ الآن يتم تمرير القائمة المحدثة
                onNavigateUp = { navController.popBackStack() },
                onPublish = {
                    navController.navigate(Routes.TEACHER_HOME_SCREEN) {
                        popUpTo(Routes.TEACHER_HOME_SCREEN) { inclusive = true }
                    }
                },
                onEditQuestion = { questionToEdit, allQuestions ->
                    // 1. ضع السؤال والقائمة الكاملة في SavedStateHandle
                    // (هنا نستخدم currentBackStackEntry لأنه هو BackStackEntry الحالي لـ QUIZ_SUMMARY_SCREEN)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "questionToEdit", questionToEdit
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "allQuestions", allQuestions
                    )
                    // 2. انتقل إلى شاشة التعديل
                    navController.navigate(Routes.ADD_QUESTION_SCREEN)
                },
            )
        }
    }
}