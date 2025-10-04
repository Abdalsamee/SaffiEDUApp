package com.example.saffieduapp.presentation.screens.teacher.calsses

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.saffieduapp.presentation.screens.student.components.CommonTopAppBar
import com.example.saffieduapp.presentation.screens.teacher.calsses.components.TeacherClassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassesScreen(
    viewModel: TeacherClassesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

     fun extractClassNumber(className: String): Int {
        return when {
            className.contains("الصف الأول") -> 1
            className.contains("الصف الثاني") -> 2
            className.contains("الصف الثالث") -> 3
            className.contains("الصف الرابع") -> 4
            className.contains("الصف الخامس") -> 5
            className.contains("الصف السادس") -> 6
            className.contains("الصف السابع") -> 7
            className.contains("الصف الثامن") -> 8
            className.contains("الصف التاسع") -> 9
            className.contains("الصف العاشر") -> 10
            className.contains("الصف الحادي عشر") -> 11
            className.contains("الصف الثاني عشر") -> 12
            else -> 0
        }
    }
    Scaffold(
        topBar = { CommonTopAppBar(title = "صفوفي") }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp), // ريسبونسف: يوزع حسب عرض الشاشة
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.classes.sortedBy { extractClassNumber(it.className) }) { classItem ->
                        TeacherClassCard(
                            classItem = classItem,
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Clicked on ${classItem.className}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
