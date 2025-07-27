package com.example.saffieduapp.domain.use_case.onboarding

import com.example.saffieduapp.data.local.preferences.OnboardingPreferences
import kotlinx.coroutines.flow.Flow

class GetOnboardingCompletedUseCase(
    private val preferences: OnboardingPreferences
) {
    operator fun invoke(): Flow<Boolean> {
        return preferences.isOnboardingCompleted
    }
}
