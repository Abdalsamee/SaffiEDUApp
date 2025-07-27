package com.example.saffieduapp.domain.use_case.onboarding


import com.example.saffieduapp.data.local.preferences.OnboardingPreferences

class SetOnboardingCompletedUseCase(
    private val preferences: OnboardingPreferences
) {

    suspend operator fun invoke() {

        preferences.setOnboardingCompleted(completed = true)
    }
}
