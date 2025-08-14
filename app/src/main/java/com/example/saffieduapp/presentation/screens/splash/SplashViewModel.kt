package com.example.saffieduapp.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saffieduapp.domain.use_case.onboarding.GetOnboardingCompletedUseCase
import com.example.saffieduapp.navigation.Routes

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val getOnboardingCompletedUseCase: GetOnboardingCompletedUseCase
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        checkOnboardingStatus()
    }

    private fun checkOnboardingStatus() {
        viewModelScope.launch {

            val isOnboardingCompleted = getOnboardingCompletedUseCase().first()


            _startDestination.value = if (isOnboardingCompleted) {
                Routes.LOGIN_SCREEN
            } else {
                Routes.ONBOARDING_SCREEN
            }
        }
    }
}