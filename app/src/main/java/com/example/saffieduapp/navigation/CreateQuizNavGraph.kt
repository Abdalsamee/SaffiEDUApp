package com.example.saffieduapp.navigation

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
            // ✅ التعديل هنا: جلب QuestionData من الـ SavedStateHandle الخاص بـ *Previous* BackStackEntry
            //    لأن QuizSummaryScreen (السابقة) هي من قامت بتخزين البيانات.
            val questionToEdit =
                navController.previousBackStackEntry?.savedStateHandle?.get<QuestionData>("questionToEdit")

            // ✅ يجب إزالة المفتاح بعد استخدامه لتجنب التعديل مرة أخرى إذا انتقلنا لـ ADD_QUESTION_SCREEN بطريقة عادية
            navController.previousBackStackEntry?.savedStateHandle?.remove<QuestionData>("questionToEdit")
            AddQuestionScreen(
                navController = navController,
                questionToEdit = questionToEdit, // تمرير السؤال إلى الشاشة للتعديل
                onNavigateUp = { navController.popBackStack() },
                onNavigateToSummary = { questions ->
                    // احفظ الأسئلة في SavedStateHandle للـ QuizSummaryScreen
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "questions", questions
                    )
                    navController.navigate(Routes.QUIZ_SUMMARY_SCREEN)
                })
        }

        // الوجهة الثالثة: ملخص الأسئلة
        composable(Routes.QUIZ_SUMMARY_SCREEN) { backStackEntry ->
            // ✅ الحل هنا: استخدم remember لحفظ النتيجة
            val (examState, questions) = remember {
                // نقوم بالقراءة داخل remember لضمان حدوثها مرة واحدة فقط
                val exam =
                    navController.getBackStackEntry(Routes.ADD_EXAM_SCREEN).savedStateHandle.get<AddExamState>(
                        "examState"
                    ) ?: AddExamState()

                val qst =
                    navController.getBackStackEntry(Routes.ADD_QUESTION_SCREEN).savedStateHandle.get<List<QuestionData>>(
                        "questions"
                    ) ?: emptyList()

                // قم بإرجاع القيم كـ Pair أو كائنات Immutable
                exam to qst
            }

            QuizSummaryScreen(
                examState = examState,
                questions = questions,
                onNavigateUp = { navController.popBackStack() },
                onPublish = {
                    // ✅ العودة إلى شاشة المعلم الرئيسية بعد النشر
                    navController.navigate(Routes.TEACHER_HOME_SCREEN) {
                        popUpTo(Routes.TEACHER_HOME_SCREEN) { inclusive = true }
                    }
                },
                onEditQuestion = { questionData ->
                    // 1. **التعديل هنا:** احفظ QuestionData في SavedStateHandle الخاص بالـ Back Stack Entry الحالي.
                    //    هذا الـ SavedStateHandle هو ما ستستخدمه شاشة ADD_QUESTION_SCREEN لجلب البيانات منه.
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "questionToEdit", questionData
                    )
                    // 2. انتقل إلى شاشة إضافة الأسئلة لتعديل السؤال المحفوظ
                    navController.navigate(Routes.ADD_QUESTION_SCREEN)
                })
        }
    }
}