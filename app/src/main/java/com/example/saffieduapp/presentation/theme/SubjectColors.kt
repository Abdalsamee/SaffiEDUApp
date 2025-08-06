package com.example.saffieduapp.presentation.theme

import androidx.compose.ui.graphics.Color

fun getSubjectColor(subjectName: String): Color {
    // We use lowercase() to make the matching case-insensitive
    return when (subjectName.lowercase()) {
        "التربية الإسلامية" -> Color(0xFFC1EDCA)
        "اللغة الانجليزية", "english" -> Color(0xFFE5D3CE)
        "رياضيات" -> Color(0xFFFFEFD5)
        "اللغة العربية" -> Color(0xFFF4D9F2)

        else -> Color(0xFFF5F5F5) // Default light grey for any other subject
    }
}