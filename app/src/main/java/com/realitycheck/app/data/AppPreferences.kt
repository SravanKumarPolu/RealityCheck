package com.realitycheck.app.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App preferences manager
 * Stores app-level settings like onboarding completion
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_preferences",
        Context.MODE_PRIVATE
    )
    
    private val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    private val KEY_STREAK_GRACE_PERIOD_DAYS = "streak_grace_period_days"
    
    /**
     * Check if user has completed onboarding
     */
    fun hasCompletedOnboarding(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }
    
    /**
     * Mark onboarding as completed
     */
    fun setOnboardingCompleted(completed: Boolean = true) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
    }
    
    /**
     * Get streak grace period in days (default: 1 day)
     * Allows user to maintain streak even if they miss a day
     */
    fun getStreakGracePeriodDays(): Int {
        return prefs.getInt(KEY_STREAK_GRACE_PERIOD_DAYS, 1)
    }
    
    /**
     * Set streak grace period in days
     */
    fun setStreakGracePeriodDays(days: Int) {
        prefs.edit().putInt(KEY_STREAK_GRACE_PERIOD_DAYS, days).apply()
    }
}

