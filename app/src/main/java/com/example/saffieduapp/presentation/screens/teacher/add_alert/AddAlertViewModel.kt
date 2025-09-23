package com.example.saffieduapp.presentation.screens.teacher.add_alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.data.FireBase.Alert
import com.example.saffieduapp.data.FireBase.AlertRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddAlertViewModel @Inject constructor(
    private val repository: AlertRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddAlertState())
    val state = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onDescriptionChange(description: String) {
        _state.update { it.copy(alertDescription = description) }
    }

    fun onTargetClassChange(className: String) {
        _state.update { it.copy(targetClass = className) }
    }

    fun onSendDateChange(dateInMillis: Long) {
        val formattedDate = formatDate(dateInMillis)
        _state.update { it.copy(sendDate = formattedDate) }
    }

    fun onSendTimeChange(hour: Int, minute: Int) {
        val formattedTime = formatTime(hour, minute)
        _state.update { it.copy(sendTime = formattedTime) }
    }
    fun sendAlert() {
        val current = state.value
        if (current.alertDescription.isBlank() || current.targetClass.isBlank()) {
            viewModelScope.launch { _eventFlow.emit("الرجاء إدخال جميع الحقول المطلوبة") }
            return
        }

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val alert = Alert(
                description = current.alertDescription,
                targetClass = current.targetClass,
                sendDate = current.sendDate,
                sendTime = current.sendTime
            )
            val success = repository.saveAlert(alert)

            if (success) {
                _eventFlow.emit("تم حفظ التنبيه بنجاح ✅")
                _state.update { AddAlertState() } // إعادة تعيين القيم بعد الحفظ
            } else {
                _eventFlow.emit("حدث خطأ أثناء حفظ التنبيه ❌")
                _state.update { it.copy(isSaving = false) }
            }
        }
    }
    fun formatDate(date: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return sdf.format(Date(date))
    }

    fun formatTime(hour: Int, minute: Int): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.ENGLISH) // 12-hour format AM/PM
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        return sdf.format(cal.time)
    }

}
