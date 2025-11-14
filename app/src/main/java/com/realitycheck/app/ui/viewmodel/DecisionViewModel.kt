package com.realitycheck.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realitycheck.app.data.Decision
import com.realitycheck.app.data.DecisionRepository
import com.realitycheck.app.data.AnalyticsData
import com.realitycheck.app.notifications.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Date

@HiltViewModel
class DecisionViewModel @Inject constructor(
    val repository: DecisionRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    val decisions = repository.getAllDecisions()
    
    private val _analytics = MutableStateFlow<AnalyticsData?>(null)
    val analytics: StateFlow<AnalyticsData?> = _analytics.asStateFlow()
    
    private val _uiState = MutableStateFlow<DecisionUiState>(DecisionUiState.Idle)
    val uiState: StateFlow<DecisionUiState> = _uiState.asStateFlow()
    
    // Cache for analytics to avoid recalculating on every update
    private var cachedAnalytics: AnalyticsData? = null
    private var lastUpdateTime: Long = 0
    private val cacheTimeoutMs = 2000L // Cache for 2 seconds
    
    init {
        viewModelScope.launch {
            try {
                decisions.collect { decisionsList ->
                    try {
                        val now = System.currentTimeMillis()
                        
                        // Use cache if available and recent
                        if (cachedAnalytics != null && 
                            now - lastUpdateTime < cacheTimeoutMs &&
                            cachedAnalytics!!.decisions.size == decisionsList.size) {
                            // Only update if decision count changed
                            val decisionCountChanged = cachedAnalytics!!.decisions.size != decisionsList.size
                            if (!decisionCountChanged) {
                                return@collect
                            }
                        }
                        
                        // Recalculate analytics
                        val completed = decisionsList.filter { it.isCompleted() }
                        val total = decisionsList.size
                        val avgAccuracy = if (completed.isNotEmpty()) {
                            completed.mapNotNull { it.getAccuracy() }.average().toFloat()
                        } else 0f
                        
                        val newAnalytics = AnalyticsData(
                            totalDecisions = total,
                            completedDecisions = completed.size,
                            averageAccuracy = avgAccuracy,
                            decisions = decisionsList
                        )
                        
                        // Update cache
                        cachedAnalytics = newAnalytics
                        lastUpdateTime = now
                        _analytics.value = newAnalytics
                    } catch (e: Exception) {
                        // Log error but don't crash - analytics is non-critical
                        // In production, use proper logging
                        _analytics.value = AnalyticsData(
                            totalDecisions = 0,
                            completedDecisions = 0,
                            averageAccuracy = 0f,
                            decisions = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle flow collection errors
                _analytics.value = AnalyticsData(
                    totalDecisions = 0,
                    completedDecisions = 0,
                    averageAccuracy = 0f,
                    decisions = emptyList()
                )
            }
        }
    }
    
    fun createDecision(
        title: String,
        description: String = "",
        prediction: String,
        reminderDays: Int,
        category: String? = null,
        energy24h: Float? = null,
        mood24h: Float? = null,
        stress24h: Float? = null,
        regretChance24h: Float? = null,
        overallImpact7d: Float? = null,
        confidence: Float? = null,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                // Input validation
                val validationError = validateDecisionInput(
                    title = title,
                    category = category,
                    energy24h = energy24h,
                    mood24h = mood24h,
                    stress24h = stress24h,
                    regretChance24h = regretChance24h,
                    overallImpact7d = overallImpact7d,
                    confidence = confidence,
                    reminderDays = reminderDays
                )
                
                if (validationError != null) {
                    _uiState.value = DecisionUiState.Error(validationError)
                    return@launch
                }
                
                val reminderDate = if (reminderDays > 0) {
                    Date(System.currentTimeMillis() + (reminderDays * 24 * 60 * 60 * 1000L))
                } else null
                
                val decision = Decision(
                    title = title.trim(),
                    description = description.trim(),
                    prediction = prediction.trim(),
                    createdAt = Date(),
                    reminderDate = reminderDate,
                    category = category,
                    predictedEnergy24h = energy24h,
                    predictedMood24h = mood24h,
                    predictedStress24h = stress24h,
                    predictedRegretChance24h = regretChance24h,
                    predictedOverallImpact7d = overallImpact7d,
                    predictionConfidence = confidence,
                    tags = tags
                )
                
                val insertedId = repository.insertDecision(decision)
                
                // Schedule notification
                val insertedDecision = repository.getDecisionById(insertedId)
                insertedDecision?.let { decision ->
                    NotificationScheduler.scheduleNotification(context, decision)
                }
                
                _uiState.value = DecisionUiState.Success
            } catch (e: IllegalArgumentException) {
                _uiState.value = DecisionUiState.Error(e.message ?: "Invalid input")
            } catch (e: Exception) {
                _uiState.value = DecisionUiState.Error(
                    "Failed to create decision: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    private fun validateDecisionInput(
        title: String,
        category: String?,
        energy24h: Float?,
        mood24h: Float?,
        stress24h: Float?,
        regretChance24h: Float?,
        overallImpact7d: Float?,
        confidence: Float?,
        reminderDays: Int
    ): String? {
        // Validate title
        if (title.isBlank()) {
            return "Title cannot be empty"
        }
        if (title.trim().length > 200) {
            return "Title is too long (maximum 200 characters)"
        }
        
        // Validate category
        if (category.isNullOrBlank()) {
            return "Please select a category"
        }
        if (category !in Decision.CATEGORIES) {
            return "Invalid category selected"
        }
        
        // Validate slider ranges (-5 to +5)
        val range = -5f..5f
        if (energy24h != null && energy24h !in range) {
            return "Energy value must be between -5 and +5"
        }
        if (mood24h != null && mood24h !in range) {
            return "Mood value must be between -5 and +5"
        }
        if (stress24h != null && stress24h !in range) {
            return "Stress value must be between -5 and +5"
        }
        if (regretChance24h != null && regretChance24h !in range) {
            return "Regret chance value must be between -5 and +5"
        }
        if (overallImpact7d != null && overallImpact7d !in range) {
            return "Overall impact value must be between -5 and +5"
        }
        
        // Validate confidence (0-100)
        if (confidence != null && (confidence < 0f || confidence > 100f)) {
            return "Confidence must be between 0 and 100"
        }
        
        // Validate reminder days
        if (reminderDays < 0 || reminderDays > 365) {
            return "Reminder days must be between 0 and 365"
        }
        
        return null // Validation passed
    }
    
    fun updateDecisionOutcome(
        decision: Decision,
        outcome: String = "",
        actualEnergy24h: Float? = null,
        actualMood24h: Float? = null,
        actualStress24h: Float? = null,
        actualRegret24h: Float? = null,
        followedDecision: Boolean? = null
    ) {
        viewModelScope.launch {
            try {
                // Input validation
                val validationError = validateOutcomeInput(
                    actualEnergy24h = actualEnergy24h,
                    actualMood24h = actualMood24h,
                    actualStress24h = actualStress24h,
                    actualRegret24h = actualRegret24h,
                    followedDecision = followedDecision,
                    decision = decision
                )
                
                if (validationError != null) {
                    _uiState.value = DecisionUiState.Error(validationError)
                    return@launch
                }
                
                val updated = decision.copy(
                    outcome = outcome.trim().ifBlank { null },
                    outcomeRecordedAt = Date(),
                    actualEnergy24h = actualEnergy24h,
                    actualMood24h = actualMood24h,
                    actualStress24h = actualStress24h,
                    actualRegret24h = actualRegret24h,
                    followedDecision = followedDecision
                )
                
                repository.updateDecision(updated)
                _uiState.value = DecisionUiState.Success
            } catch (e: IllegalArgumentException) {
                _uiState.value = DecisionUiState.Error(e.message ?: "Invalid input")
            } catch (e: Exception) {
                _uiState.value = DecisionUiState.Error(
                    "Failed to update decision: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    private fun validateOutcomeInput(
        actualEnergy24h: Float?,
        actualMood24h: Float?,
        actualStress24h: Float?,
        actualRegret24h: Float?,
        followedDecision: Boolean?,
        decision: Decision
    ): String? {
        // Validate that decision exists
        if (decision.id == 0L) {
            return "Invalid decision"
        }
        
        // Validate slider ranges (-5 to +5 for energy, mood, stress)
        val range = -5f..5f
        if (actualEnergy24h != null && actualEnergy24h !in range) {
            return "Energy value must be between -5 and +5"
        }
        if (actualMood24h != null && actualMood24h !in range) {
            return "Mood value must be between -5 and +5"
        }
        if (actualStress24h != null && actualStress24h !in range) {
            return "Stress value must be between -5 and +5"
        }
        
        // Validate regret (0-10)
        if (actualRegret24h != null && (actualRegret24h < 0f || actualRegret24h > 10f)) {
            return "Regret value must be between 0 and 10"
        }
        
        // Validate that at least one outcome field is provided or followedDecision is set
        val hasOutcomeData = actualEnergy24h != null || 
                            actualMood24h != null || 
                            actualStress24h != null || 
                            actualRegret24h != null ||
                            followedDecision != null
        
        if (!hasOutcomeData) {
            return "Please provide at least one outcome value or indicate if you followed the decision"
        }
        
        return null // Validation passed
    }
    
    fun deleteDecision(decision: Decision) {
        viewModelScope.launch {
            try {
                if (decision.id == 0L) {
                    _uiState.value = DecisionUiState.Error("Cannot delete invalid decision")
                    return@launch
                }
                repository.deleteDecision(decision)
                _uiState.value = DecisionUiState.Success
            } catch (e: Exception) {
                _uiState.value = DecisionUiState.Error(
                    "Failed to delete decision: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }
    
    fun resetUiState() {
        _uiState.value = DecisionUiState.Idle
    }
}

sealed class DecisionUiState {
    object Idle : DecisionUiState()
    object Success : DecisionUiState()
    data class Error(val message: String) : DecisionUiState()
}

