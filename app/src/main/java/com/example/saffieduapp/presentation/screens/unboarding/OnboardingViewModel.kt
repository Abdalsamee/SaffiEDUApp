package com.example.saffieduapp.presentation.screens.unboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.use_case.onboarding.SetOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val setOnboardingCompletedUseCase: SetOnboardingCompletedUseCase
) : ViewModel() {


    fun onFinishClick() {

        viewModelScope.launch {

            setOnboardingCompletedUseCase()
        }
    }
}
