package com.example.saffieduapp.presentation.screens.teacher.add_assignment

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AddAssignmentViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AddAssignmentState())
    val state = _state.asStateFlow()

    fun onEvent(event: AddAssignmentEvent) {
        when (event) {
            is AddAssignmentEvent.TitleChanged -> {
                _state.update { it.copy(title = event.title) }
            }
            is AddAssignmentEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
            }
            is AddAssignmentEvent.DateChanged -> {
                _state.update { it.copy(dueDate = event.date) }
            }
            is AddAssignmentEvent.ClassSelected -> {
                _state.update { it.copy(selectedClass = event.className) }
            }
            is AddAssignmentEvent.ImageSelected -> {
                _state.update {
                    it.copy(
                        selectedImageUri = event.uri,
                        selectedImageName = event.uri?.let { uri -> getFileName(uri) }
                    )
                }
            }
            is AddAssignmentEvent.SaveClicked -> {
                // TODO: Add save logic
                println("Saving assignment: ${state.value}")
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
}