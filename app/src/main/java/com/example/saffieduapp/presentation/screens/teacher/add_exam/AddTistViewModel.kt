package com.example.saffieduapp.presentation.screens.teacher.add_exam
// File: app/src/main/java/com/example/saffieduapp/presentation/viewmodel/AddTestViewModel.kt

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.saffieduapp.data.model.TestData
//import com.example.saffieduapp.data.model.TestType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // استيراد SimpleDateFormat
import java.util.Calendar // تأكد من استيراد Calendar
import java.util.Locale // استيراد Locale

class AddTestViewModel : ViewModel() {

    // تهيئة التاريخ الافتراضي ليكون اليوم الحالي بصيغة dd/MM/yyyy
    private val defaultDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val defaultDateString = defaultDateFormatter.format(Calendar.getInstance().time)


    private val _testData = mutableStateOf(TestData(dateString = defaultDateString)) // <<<< تحديث هنا
    val testData: State<TestData> = _testData

    // دوال لمعالجة تغييرات المستخدم وتحديث حالة testData

    fun onClassNameChanged(name: String) {
        _testData.value = _testData.value.copy(className = name)
        println("Class changed to: $name")
    }

    fun onTitleChanged(newTitle: String) {
        _testData.value = _testData.value.copy(title = newTitle)
        println("Title changed to: $newTitle")
    }

    fun onTestTypeChanged(newType: TestType) {
        _testData.value = _testData.value.copy(type = newType)
        println("Test Type changed to: ${newType.displayName}")
    }

    // <<<< تحديث دالة التاريخ للتعامل مع String
    fun onDateChanged(dateString: String) {
        _testData.value = _testData.value.copy(dateString = dateString)
        println("Date changed to: $dateString")
    }

    fun onDurationChanged(durationMinutes: Int) {
        _testData.value = _testData.value.copy(durationMinutes = durationMinutes)
        println("Duration changed to: $durationMinutes minutes")
    }

    fun onShuffleQuestionsToggled(isEnabled: Boolean) {
        _testData.value = _testData.value.copy(shuffleQuestions = isEnabled)
        println("Shuffle questions toggled: $isEnabled")
    }

    fun onShowResultsImmediatelyToggled(isEnabled: Boolean) {
        _testData.value = _testData.value.copy(showResultsImmediately = isEnabled)
        println("Show results immediately toggled: $isEnabled")
    }

    // دوال لمعالجة إجراءات الأزرار

    fun saveAsDraft() {
        viewModelScope.launch {
            println("Saving test as draft: ${_testData.value}")
            // TODO: Implement actual saving to a local database or server as a draft
            delay(1000)
            println("Draft saved!")
            // يمكن إظهار رسالة تأكيد للمستخدم
        }
    }

    fun goToNextStep() {
        viewModelScope.launch {
            println("Proceeding to next step with test: ${_testData.value}")
            // TODO: Implement validation for _testData.value
            // TODO: Navigate to the next screen (e.g., Add Questions Screen)
            delay(500)
            println("Navigating to next step.")
        }
    }
}