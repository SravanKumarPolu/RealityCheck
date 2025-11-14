package com.realitycheck.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "decisions")
data class Decision(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val prediction: String,
    val outcome: String? = null,
    val createdAt: Date,
    val reminderDate: Date? = null,
    val outcomeRecordedAt: Date? = null,
    val category: String? = null,
    // Quantitative short-term predictions (next 24 hours): -5 to +5
    val predictedEnergy24h: Float? = null,
    val predictedMood24h: Float? = null,
    val predictedStress24h: Float? = null,
    val predictedRegretChance24h: Float? = null,
    // Long-term prediction (next 7 days): -5 to +5
    val predictedOverallImpact7d: Float? = null,
    // Confidence in prediction: 0-100
    val predictionConfidence: Float? = null,
    // Quantitative actual outcomes (recorded at check-in)
    val actualEnergy24h: Float? = null,
    val actualMood24h: Float? = null,
    val actualStress24h: Float? = null,
    val actualRegret24h: Float? = null, // 0-10 scale
    // Did user follow through with the decision?
    val followedDecision: Boolean? = null,
    // Tags for better organization
    val tags: List<String> = emptyList(),
    // Group/Project ID for organization
    val groupId: Long? = null
) {
    fun isCompleted(): Boolean = outcome != null || actualEnergy24h != null
    
    /**
     * Calculate accuracy based on quantitative predictions vs actuals
     * Returns percentage (0-100) where 100% = perfect match
     */
    fun getAccuracy(): Float? {
        // Prefer quantitative accuracy if available
        if (predictedEnergy24h != null && actualEnergy24h != null) {
            return calculateQuantitativeAccuracy()
        }
        
        // Fall back to text-based if quantitative not available
        if (outcome == null || prediction.isEmpty()) return null
        
        // Simple semantic similarity - in a real app, you'd use NLP
        val predictionLower = prediction.lowercase()
        val outcomeLower = outcome.lowercase()
        
        val predictionWords = predictionLower.split(Regex("\\s+")).toSet()
        val outcomeWords = outcomeLower.split(Regex("\\s+")).toSet()
        
        if (predictionWords.isEmpty() || outcomeWords.isEmpty()) return null
        
        val intersection = predictionWords.intersect(outcomeWords).size
        val union = predictionWords.union(outcomeWords).size
        
        return if (union > 0) (intersection.toFloat() / union) * 100f else 0f
    }
    
    /**
     * Calculate accuracy from quantitative predictions vs actuals
     * Uses Mean Absolute Error (MAE) converted to accuracy percentage
     */
    private fun calculateQuantitativeAccuracy(): Float {
        var totalError = 0f
        var metricsCount = 0
        
        // Energy prediction (24h) - scale -5 to +5
        if (predictedEnergy24h != null && actualEnergy24h != null) {
            val error = kotlin.math.abs(predictedEnergy24h - actualEnergy24h)
            totalError += error
            metricsCount++
        }
        
        // Mood prediction (24h) - scale -5 to +5
        if (predictedMood24h != null && actualMood24h != null) {
            val error = kotlin.math.abs(predictedMood24h - actualMood24h)
            totalError += error
            metricsCount++
        }
        
        // Stress prediction (24h) - scale -5 to +5
        if (predictedStress24h != null && actualStress24h != null) {
            val error = kotlin.math.abs(predictedStress24h - actualStress24h)
            totalError += error
            metricsCount++
        }
        
        // Regret chance - different scales: predicted is -5 to +5, actual is 0-10
        // Normalize predicted to 0-10 scale for comparison
        if (predictedRegretChance24h != null && actualRegret24h != null) {
            val predictedNormalized = predictedRegretChance24h + 5f // Convert -5..+5 to 0..10
            val error = kotlin.math.abs(predictedNormalized - actualRegret24h)
            totalError += error
            metricsCount++
        }
        
        if (metricsCount == 0) return 0f
        
        val averageError = totalError / metricsCount
        
        // Convert error to accuracy percentage
        // For -5 to +5 scale: max error is 10, perfect accuracy = 0 error
        // Accuracy = 100% - (averageError / maxPossibleError) * 100
        // For most metrics (scale -5 to +5): max error = 10
        // For regret (normalized 0-10): max error = 10
        val maxPossibleError = 10f
        val accuracy = 100f - ((averageError / maxPossibleError) * 100f)
        
        return accuracy.coerceIn(0f, 100f)
    }
    
    /**
     * Calculate Regret Index: 0-100 scale
     * Based on actual regret (0-10), whether decision was followed, and outcome
     */
    fun getRegretIndex(): Float? {
        if (actualRegret24h == null) return null
        
        var regretScore = actualRegret24h // 0-10
        
        // Penalize if they didn't follow through (adds to regret)
        if (followedDecision == false) {
            regretScore += 2f // Add 2 points for not following
        }
        
        // If they followed but regret is still high, that's significant
        if (followedDecision == true && actualRegret24h >= 7f) {
            regretScore += 1f // Extra point for following through but still high regret
        }
        
        // Normalize to 0-100 scale (regret score is 0-13 after adjustments)
        val regretIndex = (regretScore / 13f) * 100f
        
        return regretIndex.coerceIn(0f, 100f)
    }
    
    /**
     * Returns time until check-in in hours, or null if no reminder date or already completed
     */
    fun getHoursUntilCheckIn(): Long? {
        if (isCompleted() || reminderDate == null) return null
        val now = Date()
        if (reminderDate.before(now)) return 0L
        val diff = reminderDate.time - now.time
        return diff / (1000 * 60 * 60) // Convert to hours
    }
    
    /**
     * Returns formatted string for time until check-in
     * e.g., "Check-in in 23h" or "Check-in overdue"
     */
    fun getCheckInTimeString(): String? {
        val hours = getHoursUntilCheckIn() ?: return null
        return if (hours <= 0) "Check-in overdue" else "Check-in in ${hours}h"
    }
    
    /**
     * Returns emoji indicator for completed decisions
     * ✅ for good decisions (low regret index or high accuracy), 😬 for regret (high regret index)
     */
    fun getRegretIndicator(): String? {
        if (!isCompleted()) return null
        
        // Prefer regret index over accuracy for emotional indicator
        val regretIndex = getRegretIndex()
        if (regretIndex != null) {
            return if (regretIndex < 50) "✅" else "😬"
        }
        
        // Fall back to accuracy
        val accuracy = getAccuracy() ?: return "😬"
        return if (accuracy >= 60) "✅" else "😬"
    }
    
    /**
     * Returns display category, defaults to "Other" if not set
     */
    fun getDisplayCategory(): String {
        return category ?: "Other"
    }
    
    companion object {
        val CATEGORIES = listOf("Health", "Money", "Work", "Study", "Relationships", "Habits", "Other")
        
        /**
         * Returns default check-in days based on category
         */
        fun getDefaultCheckInDays(category: String?): Int {
            return when (category) {
                "Health" -> 1
                "Money" -> 7
                "Work" -> 3
                "Study" -> 3
                "Relationships" -> 7
                "Habits" -> 7
                else -> 3 // Default
            }
        }
        
        /**
         * Returns check-in options for the UI
         */
        val CHECK_IN_OPTIONS = listOf(
            "Tomorrow" to 1,
            "In 3 days" to 3,
            "In 7 days" to 7
        )
    }
}

