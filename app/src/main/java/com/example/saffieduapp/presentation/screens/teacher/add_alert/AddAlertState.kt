package com.example.saffieduapp.presentation.screens.teacher.add_alert

data class AddAlertState(
    val alertDescription: String = "",
    val targetClass: String = "", // الصف المستهدف
    val sendDate: String = "",
    val sendTime: String = "",
    val isSending: Boolean = false
)