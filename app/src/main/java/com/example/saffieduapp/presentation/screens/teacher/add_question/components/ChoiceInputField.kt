package com.example.saffieduapp.presentation.screens.teacher.add_question.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saffieduapp.R
import com.example.saffieduapp.presentation.screens.teacher.add_question.AddQuestionEvent
import com.example.saffieduapp.presentation.screens.teacher.add_question.Choice
import com.example.saffieduapp.presentation.screens.teacher.add_question.QuestionType
import com.example.saffieduapp.ui.theme.AppAccent
import com.example.saffieduapp.ui.theme.AppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceInputField(
    choice: Choice,
    questionType: QuestionType,
    onEvent: (AddQuestionEvent) -> Unit,
    canBeDeleted: Boolean,
    modifier: Modifier = Modifier
) {
    // استخدام key لكل choice.id لضمان عدم الخلط
    var text by remember(choice.id) { mutableStateOf(choice.text) }
    val focusManager = LocalFocusManager.current

    // تحديث النص المحلي عندما يتغير choice.text من الخارج
    LaunchedEffect(choice.text) {
        text = choice.text
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 20.dp
                )
            )
            .background(Color.White, shape = RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 20.dp,
                bottomEnd = 0.dp,
                bottomStart = 20.dp
            ))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (questionType) {
            QuestionType.MULTIPLE_CHOICE_SINGLE, QuestionType.TRUE_FALSE -> {
                RadioButton(
                    selected = choice.isCorrect,
                    onClick = {
                        onEvent(AddQuestionEvent.CorrectChoiceSelected(choice.id))
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppAccent,          // لون الدائرة عند التحديد
                        unselectedColor = Color.Gray,       // لون الدائرة عند عدم التحديد
                        disabledSelectedColor = Color.LightGray,
                        disabledUnselectedColor = Color.DarkGray
                    )
                )
            }
            QuestionType.MULTIPLE_CHOICE_MULTIPLE -> {
                Checkbox(
                    checked = choice.isCorrect,
                    onCheckedChange = {
                        onEvent(AddQuestionEvent.CorrectChoiceSelected(choice.id))
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppAccent,         // لون المربع عند التحديد
                        uncheckedColor = Color.Gray,        // لون المربع عند عدم التحديد
                        checkmarkColor = Color.White,       // لون علامة الصح
                        disabledCheckedColor = Color.LightGray,
                        disabledUncheckedColor = Color.DarkGray
                    )
                )
            }
            else -> {}
        }

        TextField(
            value = text,
            onValueChange = { newText -> text = newText },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp)
                .onFocusChanged { focusState ->
                    // تحديث الـ ViewModel فقط عند فقدان التركيز وإذا تغير النص
                    if (!focusState.isFocused && text != choice.text) {
                        onEvent(AddQuestionEvent.ChoiceTextChanged(choice.id, text))
                    }
                },
            placeholder = { Text("الخيار", color = Color.Gray, fontSize = 14.sp) },
            readOnly = questionType == QuestionType.TRUE_FALSE,
            colors = TextFieldDefaults.textFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            trailingIcon = {
                if (canBeDeleted) {
                    IconButton(onClick = {
                        onEvent(AddQuestionEvent.RemoveChoiceClicked(choice.id))
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Remove choice",
                            tint = Color.Unspecified
                        )
                    }
                }
            }
        )
    }
}