package com.example.saffieduapp.presentation.screens.teacher.add_alert

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddAlertViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(AddAlertState())
    val state = _state.asStateFlow()

    fun onDescriptionChange(description: String) {
        _state.update { it.copy(alertDescription = description) }
    }

    // ... دوال أخرى لتحديث بقية الحقول

    fun sendAlert() {
        // TODO: Add logic to validate and send the alert via Firebase
        println("Sending Alert: ${state.value}")
    }
}