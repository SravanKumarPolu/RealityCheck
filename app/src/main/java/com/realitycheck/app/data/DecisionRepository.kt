package com.realitycheck.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DecisionRepository @Inject constructor(
    private val decisionDao: DecisionDao,
    private val decisionGroupDao: DecisionGroupDao
) {
    fun getAllDecisions(): Flow<List<Decision>> = try {
        decisionDao.getAllDecisions()
    } catch (e: Exception) {
        // Return empty flow on error to prevent crashes
        kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    suspend fun getDecisionById(id: Long): Decision? = try {
        if (id <= 0) {
            null
        } else {
            decisionDao.getDecisionById(id)
        }
    } catch (e: Exception) {
        null
    }
    
    fun getCompletedDecisions(): Flow<List<Decision>> = try {
        decisionDao.getCompletedDecisions()
    } catch (e: Exception) {
        kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    fun getCompletionRate(): Flow<Float> = try {
        decisionDao.getCompletionRate()
    } catch (e: Exception) {
        kotlinx.coroutines.flow.flowOf(0f)
    }
    
    suspend fun insertDecision(decision: Decision): Long {
        if (decision.title.isBlank()) {
            throw IllegalArgumentException("Decision title cannot be blank")
        }
        return try {
            decisionDao.insertDecision(decision)
        } catch (e: Exception) {
            throw RuntimeException("Failed to insert decision: ${e.message}", e)
        }
    }
    
    suspend fun updateDecision(decision: Decision) {
        if (decision.id == 0L) {
            throw IllegalArgumentException("Cannot update decision with invalid ID")
        }
        try {
            decisionDao.updateDecision(decision)
        } catch (e: Exception) {
            throw RuntimeException("Failed to update decision: ${e.message}", e)
        }
    }
    
    suspend fun deleteDecision(decision: Decision) {
        if (decision.id == 0L) {
            throw IllegalArgumentException("Cannot delete decision with invalid ID")
        }
        try {
            decisionDao.deleteDecision(decision)
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete decision: ${e.message}", e)
        }
    }
    
    suspend fun getAnalytics(): AnalyticsData {
        val allDecisions = getAllDecisions()
        // This will be handled in ViewModel with flow collection
        return AnalyticsData(
            totalDecisions = 0,
            completedDecisions = 0,
            averageAccuracy = 0f,
            decisions = emptyList()
        )
    }
    
    fun getDecisionsByCategory(category: String): Flow<List<Decision>> = try {
        decisionDao.getDecisionsByCategory(category)
    } catch (e: Exception) {
        kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    suspend fun getAllCategories(): List<String> = try {
        decisionDao.getAllCategories()
    } catch (e: Exception) {
        emptyList()
    }
    
    suspend fun getAllTags(): List<String> = try {
        val tagsStrings = decisionDao.getAllTags()
        tagsStrings.flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    } catch (e: Exception) {
        emptyList()
    }
    
    fun filterDecisions(
        decisions: List<Decision>,
        selectedCategory: String? = null,
        selectedTags: List<String> = emptyList(),
        dateRange: Pair<Date?, Date?>? = null
    ): List<Decision> {
        return decisions.filter { decision ->
            // Category filter
            val categoryMatch = selectedCategory == null || decision.category == selectedCategory
            
            // Tags filter
            val tagsMatch = selectedTags.isEmpty() || 
                           selectedTags.any { tag -> decision.tags.contains(tag, ignoreCase = true) }
            
            // Date range filter
            val dateMatch = dateRange == null || dateRange.first == null || dateRange.second == null ||
                           (decision.createdAt.after(dateRange.first) && decision.createdAt.before(dateRange.second))
            
            categoryMatch && tagsMatch && dateMatch
        }
    }
    
    // Decision Group methods
    fun getAllGroups(): Flow<List<DecisionGroup>> = try {
        decisionGroupDao.getAllGroups()
    } catch (e: Exception) {
        kotlinx.coroutines.flow.flowOf(emptyList())
    }
    
    suspend fun getGroupById(id: Long): DecisionGroup? = try {
        if (id <= 0) {
            null
        } else {
            decisionGroupDao.getGroupById(id)
        }
    } catch (e: Exception) {
        null
    }
    
    suspend fun insertGroup(group: DecisionGroup): Long {
        if (group.name.isBlank()) {
            throw IllegalArgumentException("Group name cannot be blank")
        }
        return try {
            decisionGroupDao.insertGroup(group)
        } catch (e: Exception) {
            throw RuntimeException("Failed to insert group: ${e.message}", e)
        }
    }
    
    suspend fun updateGroup(group: DecisionGroup) {
        if (group.id == 0L) {
            throw IllegalArgumentException("Group ID must be valid")
        }
        if (group.name.isBlank()) {
            throw IllegalArgumentException("Group name cannot be blank")
        }
        try {
            decisionGroupDao.updateGroup(group.copy(updatedAt = java.util.Date()))
        } catch (e: Exception) {
            throw RuntimeException("Failed to update group: ${e.message}", e)
        }
    }
    
    suspend fun deleteGroup(group: DecisionGroup) {
        if (group.id == 0L) {
            throw IllegalArgumentException("Group ID must be valid")
        }
        try {
            // First, remove groupId from all decisions in this group
            val decisions = decisionDao.getDecisionsByGroup(group.id).first()
            decisions.forEach { decision ->
                val updatedDecision = decision.copy(groupId = null)
                decisionDao.updateDecision(updatedDecision)
            }
            // Then delete the group
            decisionGroupDao.deleteGroup(group)
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete group: ${e.message}", e)
        }
    }
    
    suspend fun getDecisionCountForGroup(groupId: Long): Int {
        return try {
            decisionGroupDao.getDecisionCountForGroup(groupId)
        } catch (e: Exception) {
            0
        }
    }
    
    fun getDecisionsByGroup(groupId: Long): Flow<List<Decision>> = try {
        decisionDao.getDecisionsByGroup(groupId)
    } catch (e: Exception) {
        kotlinx.coroutines.flow.flowOf(emptyList())
    }
}

data class AnalyticsData(
    val totalDecisions: Int,
    val completedDecisions: Int,
    val averageAccuracy: Float,
    val decisions: List<Decision>
) {
    // Cache for expensive calculations
    private var cachedRegretPatterns: Map<String, Float>? = null
    private var cachedTimeTrends: List<WeeklyTrend>? = null
    private var cachedOverconfidencePatterns: List<OverconfidencePattern>? = null
    private var cachedBlindSpots: List<BlindSpot>? = null
    private var cachedCategoryAccuracy: Map<String, Float>? = null
    private var cachedDecisionStreak: Int? = null
    private var cachedDecisionHash: Int = 0
    
    /**
     * Invalidate cache when decisions change
     */
    private fun invalidateCacheIfNeeded() {
        val currentHash = decisions.hashCode()
        if (currentHash != cachedDecisionHash) {
            cachedRegretPatterns = null
            cachedTimeTrends = null
            cachedOverconfidencePatterns = null
            cachedBlindSpots = null
            cachedCategoryAccuracy = null
            cachedDecisionStreak = null
            cachedDecisionHash = currentHash
        }
    }
    fun getRegretPatterns(): Map<String, Float> {
        invalidateCacheIfNeeded()
        if (cachedRegretPatterns != null) {
            return cachedRegretPatterns!!
        }
        
        val completed = decisions.filter { it.isCompleted() && it.getAccuracy() != null }
        if (completed.isEmpty()) {
            cachedRegretPatterns = emptyMap()
            return emptyMap()
        }
        
        val lowAccuracy = completed.filter { (it.getAccuracy() ?: 0f) < 50f }
        val mediumAccuracy = completed.filter { (it.getAccuracy() ?: 0f) in 50f..75f }
        val highAccuracy = completed.filter { (it.getAccuracy() ?: 0f) > 75f }
        
        val result = mapOf(
            "Low" to (lowAccuracy.size.toFloat() / completed.size) * 100f,
            "Medium" to (mediumAccuracy.size.toFloat() / completed.size) * 100f,
            "High" to (highAccuracy.size.toFloat() / completed.size) * 100f
        )
        cachedRegretPatterns = result
        return result
    }
    
    // Time-based trends: accuracy over weeks
    fun getTimeBasedTrends(): List<WeeklyTrend> {
        invalidateCacheIfNeeded()
        if (cachedTimeTrends != null) {
            return cachedTimeTrends!!
        }
        
        val completed = decisions.filter { it.isCompleted() && it.getAccuracy() != null && it.outcomeRecordedAt != null }
        if (completed.isEmpty()) {
            cachedTimeTrends = emptyList()
            return emptyList()
        }
        
        val groupedByWeek = completed.groupBy { decision ->
            val calendar = java.util.Calendar.getInstance().apply {
                time = decision.outcomeRecordedAt ?: decision.createdAt
            }
            val weekOfYear = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
            val year = calendar.get(java.util.Calendar.YEAR)
            "$year-W$weekOfYear"
        }
        
        val result = groupedByWeek.map { (weekKey, weekDecisions) ->
            val avgAccuracy = weekDecisions.mapNotNull { it.getAccuracy() }.average().toFloat()
            WeeklyTrend(
                weekLabel = weekKey,
                averageAccuracy = avgAccuracy,
                decisionCount = weekDecisions.size
            )
        }.sortedBy { it.weekLabel }
        cachedTimeTrends = result
        return result
    }
    
    // Overconfidence detection: decisions with very specific predictions but low accuracy
    fun getOverconfidencePatterns(): List<OverconfidencePattern> {
        invalidateCacheIfNeeded()
        if (cachedOverconfidencePatterns != null) {
            return cachedOverconfidencePatterns!!
        }
        
        val completed = decisions.filter { it.isCompleted() && it.getAccuracy() != null }
        if (completed.isEmpty()) {
            cachedOverconfidencePatterns = emptyList()
            return emptyList()
        }
        
        // High specificity = longer prediction text, more details
        // Low accuracy despite specificity = overconfidence
        val result = completed
            .mapNotNull { decision ->
                val accuracy = decision.getAccuracy() ?: return@mapNotNull null
                val specificity = decision.prediction.length.toFloat() // Simple: longer = more specific
                val isOverconfident = specificity > 50 && accuracy < 50
                
                if (isOverconfident) {
                    OverconfidencePattern(
                        decisionTitle = decision.title,
                        prediction = decision.prediction,
                        outcome = decision.outcome ?: "",
                        accuracy = accuracy,
                        specificity = specificity
                    )
                } else null
            }
            .sortedByDescending { it.specificity }
            .take(5)
        cachedOverconfidencePatterns = result
        return result
    }
    
    // Blind spots: patterns in low-accuracy decisions
    fun getBlindSpots(): List<BlindSpot> {
        invalidateCacheIfNeeded()
        if (cachedBlindSpots != null) {
            return cachedBlindSpots!!
        }
        
        val lowAccuracyDecisions = decisions.filter { 
            it.isCompleted() && 
            (it.getAccuracy() ?: 0f) < 50f 
        }
        
        if (lowAccuracyDecisions.isEmpty()) {
            cachedBlindSpots = emptyList()
            return emptyList()
        }
        
        // Analyze common words/patterns in low-accuracy predictions
        val allLowAccuracyWords = lowAccuracyDecisions
            .flatMap { decision ->
                decision.prediction.lowercase()
                    .split(Regex("\\s+"))
                    .filter { it.length > 3 } // Only meaningful words
            }
            .groupingBy { it }
            .eachCount()
        
        val result = allLowAccuracyWords
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { BlindSpot(
                pattern = it.first,
                frequency = it.second,
                impact = "Appears in ${it.second} low-accuracy predictions"
            ) }
        cachedBlindSpots = result
        return result
    }
    
    /**
     * Get most regretful category based on average regret index
     */
    fun getTopRegretCategory(): Pair<String, Float>? {
        val completed = decisions.filter { it.isCompleted() && it.getRegretIndex() != null && it.category != null }
        if (completed.isEmpty()) return null
        
        val regretByCategory = completed
            .mapNotNull { decision -> decision.category?.let { it to decision } }
            .groupBy { it.first }
            .mapValues { (_, decisionsWithCategory) ->
                decisionsWithCategory.mapNotNull { it.second.getRegretIndex() }.average().toFloat()
            }
        
        return regretByCategory.maxByOrNull { it.value }?.let { it.key to it.value }
    }
    
    /**
     * Get regret scores per category for V1 stats requirement
     * Returns map of category to average regret index (0-100)
     */
    fun getRegretScorePerCategory(): Map<String, Float> {
        val completed = decisions.filter {
            it.isCompleted() &&
            it.getRegretIndex() != null &&
            it.category != null
        }
        
        if (completed.isEmpty()) return emptyMap()
        
        return completed
            .mapNotNull { decision -> decision.category?.let { it to decision } }
            .groupBy { it.first }
            .mapValues { (_, decisionsWithCategory) ->
                decisionsWithCategory.mapNotNull { it.second.getRegretIndex() }.average().toFloat()
            }
    }
    
    /**
     * Find similar decisions to a given decision
     * Returns list of similar decisions (same category, similar title words)
     */
    fun findSimilarDecisions(decision: Decision, limit: Int = 5): List<Decision> {
        if (decision.category == null) return emptyList()
        
        val decisionWords = decision.title.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length > 3 }
            .toSet()
        
        if (decisionWords.isEmpty()) return emptyList()
        
        val similar = decisions
            .filter { 
                it.id != decision.id && 
                it.category == decision.category &&
                it.isCompleted()
            }
            .map { other ->
                val otherWords = other.title.lowercase()
                    .split(Regex("\\s+"))
                    .filter { it.length > 3 }
                    .toSet()
                
                val commonWords = decisionWords.intersect(otherWords).size
                val similarity = if (otherWords.isNotEmpty()) {
                    commonWords.toFloat() / maxOf(decisionWords.size, otherWords.size)
                } else 0f
                
                other to similarity
            }
            .filter { it.second > 0.2f } // At least 20% similarity
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
        
        return similar
    }
    
    /**
     * Get decision pattern suggestions based on past decisions
     * Returns proactive insights like "You usually regret decisions like this"
     */
    fun getDecisionPatternSuggestion(
        title: String,
        category: String?
    ): DecisionPatternSuggestion? {
        if (category == null) return null
        
        val currentWords = title.lowercase()
            .split(Regex("\\s+"))
            .filter { it.length > 3 }
            .toSet()
        
        if (currentWords.isEmpty()) return null
        
        // Find similar past decisions in the same category
        val similarDecisions = decisions
            .filter { 
                it.category == category &&
                it.isCompleted() &&
                it.id != 0 // Exclude current decision if updating
            }
            .map { past ->
                val pastWords = past.title.lowercase()
                    .split(Regex("\\s+"))
                    .filter { it.length > 3 }
                    .toSet()
                
                val commonWords = currentWords.intersect(pastWords).size
                val similarity = if (pastWords.isNotEmpty()) {
                    commonWords.toFloat() / maxOf(currentWords.size, pastWords.size)
                } else 0f
                
                past to similarity
            }
            .filter { it.second >= 0.2f } // At least 20% similarity
            .sortedByDescending { it.second }
        
        if (similarDecisions.isEmpty()) return null
        
        // Analyze patterns in similar decisions
        val highRegretSimilar = similarDecisions.filter { (decision, _) ->
            (decision.getRegretIndex() ?: 0f) >= 60f
        }
        
        val lowAccuracySimilar = similarDecisions.filter { (decision, _) ->
            (decision.getAccuracy() ?: 100f) < 50f
        }
        
        // Generate suggestion based on patterns
        return when {
            highRegretSimilar.isNotEmpty() && highRegretSimilar.size >= similarDecisions.size * 0.5f -> {
                val avgRegret = highRegretSimilar.map { it.first.getRegretIndex() ?: 0f }.average().toFloat()
                DecisionPatternSuggestion(
                    type = DecisionPatternType.HIGH_REGRET,
                    message = "You usually regret decisions like this (avg regret: ${avgRegret.toInt()}%)",
                    similarCount = highRegretSimilar.size
                )
            }
            lowAccuracySimilar.isNotEmpty() && lowAccuracySimilar.size >= similarDecisions.size * 0.5f -> {
                val avgAccuracy = lowAccuracySimilar.map { it.first.getAccuracy() ?: 0f }.average().toFloat()
                DecisionPatternSuggestion(
                    type = DecisionPatternType.LOW_ACCURACY,
                    message = "Your predictions for similar decisions have been inaccurate (avg: ${avgAccuracy.toInt()}%)",
                    similarCount = lowAccuracySimilar.size
                )
            }
            else -> {
                // Positive pattern - good accuracy or low regret
                val avgAccuracy = similarDecisions.take(5).map { it.first.getAccuracy() ?: 0f }.average().toFloat()
                if (avgAccuracy >= 70f) {
                    DecisionPatternSuggestion(
                        type = DecisionPatternType.POSITIVE_PATTERN,
                        message = "You've been accurate with similar decisions (avg: ${avgAccuracy.toInt()}%)",
                        similarCount = similarDecisions.size
                    )
                } else null
            }
        }
    }
    
    /**
     * Get repeated regret patterns - decisions with high regret that share similar titles/contexts
     */
    fun getRepeatedRegret(): String? {
        val highRegretDecisions = decisions.filter {
            it.isCompleted() && 
            it.getRegretIndex() != null && 
            (it.getRegretIndex() ?: 0f) >= 60f
        }
        
        if (highRegretDecisions.size < 2) return null
        
        // Find similar titles (simple word matching)
        val titleWords = highRegretDecisions.map { decision ->
            decision.title.lowercase().split(Regex("\\s+")).filter { it.length > 3 }.toSet() to decision.title
        }
        
        // Find common words across high-regret decisions
        val commonWords = titleWords
            .flatMap { it.first }
            .groupingBy { it }
            .eachCount()
            .filter { it.value >= 2 } // Appears in at least 2 decisions
        
        if (commonWords.isEmpty()) {
            // Fall back to category-based repeated regret
            val categoryRegret = highRegretDecisions.groupBy { it.category ?: "Other" }
            val mostCommonCategory = categoryRegret.maxByOrNull { it.value.size }
            if (mostCommonCategory != null && mostCommonCategory.value.size >= 2) {
                val sampleDecision = mostCommonCategory.value.first()
                return sampleDecision.title
            }
            return null
        }
        
        // Return a sample title that contains common words
        val sample = titleWords.firstOrNull { (words, _) ->
            words.any { commonWords.containsKey(it) }
        }?.second
        
        return sample
    }
    
    /**
     * Get miscalibration indicators - overconfidence by category
     */
    fun getOverconfidenceByCategory(): String? {
        val completed = decisions.filter { 
            it.isCompleted() && 
            it.predictionConfidence != null && 
            it.getAccuracy() != null &&
            it.category != null
        }
        
        if (completed.isEmpty()) return null
        
        // Calculate overconfidence: high confidence but low accuracy
        val categoryOverconfidence = completed
            .mapNotNull { decision -> decision.category?.let { it to decision } }
            .groupBy { it.first }
            .mapValues { (_, decisionsWithCategory) ->
                val decisions = decisionsWithCategory.map { it.second }
                val avgConfidence = decisions.mapNotNull { it.predictionConfidence }.average()
                val avgAccuracy = decisions.mapNotNull { it.getAccuracy() }.average()
                avgConfidence - avgAccuracy // Positive = overconfident
            }
            .filter { it.value > 20f } // At least 20% gap
        
        val mostOverconfident = categoryOverconfidence.maxByOrNull { it.value }
        return mostOverconfident?.key?.let { category ->
            "You are overconfident in decisions about $category."
        }
    }
    
    /**
     * Get underestimation patterns - where predictions were much better/worse than reality
     */
    fun getUnderestimationPattern(): String? {
        val completed = decisions.filter {
            it.isCompleted() &&
            it.predictedMood24h != null &&
            it.actualMood24h != null &&
            it.category != null
        }
        
        if (completed.isEmpty()) return null
        
        // Find patterns where actual mood was much worse than predicted
        val underestimations = completed.mapNotNull { decision ->
            val actualMood = decision.actualMood24h ?: return@mapNotNull null
            val predictedMood = decision.predictedMood24h ?: return@mapNotNull null
            val category = decision.category ?: return@mapNotNull null
            
            val moodDiff = actualMood - predictedMood
            if (moodDiff < -2f) { // Actual mood was at least 2 points worse
                category to moodDiff
            } else null
        }
        
        if (underestimations.isEmpty()) return null
        
        val categoryUnderestimation = underestimations.groupBy { it.first }
            .mapValues { (_, diffs) -> diffs.map { it.second }.average().toFloat() }
        
        val mostUnderestimated = categoryUnderestimation.minByOrNull { it.value }
        return mostUnderestimated?.key?.let { category ->
            when (category) {
                "Health" -> "You underestimate the impact of late-night screens on tomorrow's mood."
                else -> "You underestimate the impact of $category decisions on your mood."
            }
        }
    }
    
    /**
     * Get category accuracy comparison for bar chart
     * Returns map of category to average accuracy
     */
    fun getCategoryAccuracy(): Map<String, Float> {
        invalidateCacheIfNeeded()
        if (cachedCategoryAccuracy != null) {
            return cachedCategoryAccuracy!!
        }
        
        val completed = decisions.filter {
            it.isCompleted() &&
            it.getAccuracy() != null &&
            it.category != null
        }
        
        if (completed.isEmpty()) {
            cachedCategoryAccuracy = emptyMap()
            return emptyMap()
        }
        
        val result = completed
            .mapNotNull { decision -> decision.category?.let { it to decision } }
            .groupBy { it.first }
            .mapValues { (_, decisionsWithCategory) ->
                decisionsWithCategory.mapNotNull { it.second.getAccuracy() }.average().toFloat()
            }
        cachedCategoryAccuracy = result
        return result
    }
    
    /**
     * Calculate streak - consecutive days with at least 1 decision logged
     */
    fun getDecisionStreak(): Int {
        invalidateCacheIfNeeded()
        if (cachedDecisionStreak != null) {
            return cachedDecisionStreak!!
        }
        
        if (decisions.isEmpty()) {
            cachedDecisionStreak = 0
            return 0
        }
        
        val decisionDays = decisions.map { decision ->
            val calendar = java.util.Calendar.getInstance().apply {
                time = decision.createdAt
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            calendar.time
        }.distinct().sortedDescending()
        
        if (decisionDays.isEmpty()) {
            cachedDecisionStreak = 0
            return 0
        }
        
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        
        var streak = 0
        val calendar = java.util.Calendar.getInstance()
        
        // Check if today or yesterday has a decision
        calendar.time = today
        val todayHasDecision = decisionDays.any { day ->
            ((today.time - day.time) / (1000 * 60 * 60 * 24)) < 1
        }
        
        // Streak protection: grace period for missed check-ins
        // Check if today or yesterday has a decision (with grace period)
        val gracePeriodDays = 1 // Default grace period, can be configured via AppPreferences
        val withinGracePeriod = decisionDays.any { day ->
            val daysDiff = ((today.time - day.time) / (1000 * 60 * 60 * 24)).toInt()
            daysDiff <= gracePeriodDays // Allow grace period
        }
        
        if (!withinGracePeriod) {
            cachedDecisionStreak = 0
            return 0
        }
        
        // Count consecutive days from today backwards (with grace period)
        calendar.time = today
        var consecutiveMissedDays = 0
        while (true) {
            val dayToCheck = calendar.time
            val hasDecision = decisionDays.any { decisionDay ->
                val daysDiff = ((dayToCheck.time - decisionDay.time) / (1000 * 60 * 60 * 24)).toInt()
                daysDiff == 0 // Same day
            }
            
            if (hasDecision) {
                streak++
                consecutiveMissedDays = 0 // Reset missed days counter
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            } else {
                consecutiveMissedDays++
                if (consecutiveMissedDays > gracePeriodDays) {
                    // Streak broken - too many consecutive missed days
                    break
                }
                // Still within grace period - continue streak
                streak++
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            }
        }
        
        cachedDecisionStreak = streak
        return streak
    }
}

data class WeeklyTrend(
    val weekLabel: String,
    val averageAccuracy: Float,
    val decisionCount: Int
)

/**
 * Decision pattern suggestion for proactive insights
 */
data class DecisionPatternSuggestion(
    val type: DecisionPatternType,
    val message: String,
    val similarCount: Int
)

enum class DecisionPatternType {
    HIGH_REGRET,      // Similar decisions had high regret
    LOW_ACCURACY,     // Similar decisions had low accuracy
    POSITIVE_PATTERN  // Similar decisions had good outcomes
}

data class OverconfidencePattern(
    val decisionTitle: String,
    val prediction: String,
    val outcome: String,
    val accuracy: Float,
    val specificity: Float
)

data class BlindSpot(
    val pattern: String,
    val frequency: Int,
    val impact: String
)

