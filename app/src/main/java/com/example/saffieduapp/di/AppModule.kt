package com.example.saffieduapp.di

import android.content.Context
import com.example.saffieduapp.data.local.preferences.OnboardingPreferences
import com.example.saffieduapp.data.local.preferences.PreferencesManager
import com.example.saffieduapp.domain.use_case.onboarding.GetOnboardingCompletedUseCase
import com.example.saffieduapp.domain.use_case.onboarding.SetOnboardingCompletedUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOnboardingPreferences(@ApplicationContext context: Context): OnboardingPreferences {

        return OnboardingPreferences(context)
    }

    @Provides
    @Singleton
    fun provideGetOnboardingCompletedUseCase(preferences: OnboardingPreferences): GetOnboardingCompletedUseCase {

        return GetOnboardingCompletedUseCase(preferences)
    }

    @Provides
    @Singleton
    fun provideSetOnboardingCompletedUseCase(preferences: OnboardingPreferences): SetOnboardingCompletedUseCase {

        return SetOnboardingCompletedUseCase(preferences)
    }
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()


    @Module
    @InstallIn(SingletonComponent::class)
    object AppModule {
        @Provides
        @Singleton
        fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
            return PreferencesManager(context)
        }
    }

}