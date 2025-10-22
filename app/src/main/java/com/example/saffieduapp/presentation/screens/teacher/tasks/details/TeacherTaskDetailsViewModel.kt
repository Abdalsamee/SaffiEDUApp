    package com.example.saffieduapp.presentation.screens.teacher.tasks.details

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.launch

    class TeacherTaskDetailsViewModel : ViewModel() {

        private val _state = MutableStateFlow(TeacherTaskDetailsState())
        val state: StateFlow<TeacherTaskDetailsState> = _state

        init {
            loadFakeStudents()
        }

        private fun loadFakeStudents() {
            viewModelScope.launch {
                _state.value = _state.value.copy(isLoading = true)

                val fakeStudents = listOf(
                    StudentTaskItem("1", "يزن عادل ظهبر", "10 / 8", "https://i.pravatar.cc/150?img=1"),
                    StudentTaskItem("2", "حازم بدران", "10 / 7", "https://i.pravatar.cc/150?img=2"),
                    StudentTaskItem("3", "ادهم دالوة", "10 / 5", "https://i.pravatar.cc/150?img=3"),
                    StudentTaskItem("4", "مهند نسيب ادهم", "10 / 9", "https://i.pravatar.cc/150?img=4")
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    students = fakeStudents
                )
            }
        }

        fun onSearchChange(query: String) {
            _state.value = _state.value.copy(searchQuery = query)
        }
    }
