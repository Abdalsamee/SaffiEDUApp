//package com.example.saffieduapp.presentation.screens.SignUpScreen.components
//
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowDropDown
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.saffieduapp.ui.theme.AppPrimary
//import com.example.saffieduapp.ui.theme.AppTextPrimary
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DropDownTextField(
//    label: String,
//    placeholder: String,
//    options: List<String>,
//    selectedOption: String,
//    onOptionSelected: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded },
//        modifier = modifier
//    ) {
//        TextField(
//            value = selectedOption,
//            onValueChange = {},
//            readOnly = true,
//            label = { Text(label, fontSize = 14.sp) },
//            placeholder = { Text(placeholder, fontSize = 14.sp) },
//            trailingIcon = {
//                Icon(
//                    imageVector = Icons.Filled.ArrowDropDown,
//                    contentDescription = null
//                )
//            },
//            colors = TextFieldDefaults.textFieldColors(
//                containerColor = MaterialTheme.colorScheme.surface,
//                focusedIndicatorColor = AppPrimary,
//                focusedLabelColor = AppPrimary,
//                unfocusedIndicatorColor = AppPrimary,
//                textColor = AppTextPrimary,
//            ),
//            singleLine = true,
//            modifier = Modifier
//                .menuAnchor()
//                .fillMaxWidth()
//        )
//
//        ExposedDropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            options.forEach { selectionOption ->
//                DropdownMenuItem(
//                    text = { Text(selectionOption, fontSize = 14.sp) },
//                    onClick = {
//                        onOptionSelected(selectionOption)
//                        expanded = false
//                    },
//                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
//                )
//            }
//        }
//    }
//}
//
//private fun TextFieldDefaults.textFieldColors(
//    containerColor: Color,
//    focusedIndicatorColor: Color,
//    focusedLabelColor: Color,
//    unfocusedIndicatorColor: Color,
//    textColor: Color
//): TextFieldColors {
//    TODO("Not yet implemented")
//}
