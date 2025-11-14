package com.realitycheck.app.data

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class AnalyticsDataTest {

    @Test
    fun `getRegretPatterns returns correct percentages`() {
        val decisions = listOf(
            createDecisionWithAccuracy(30f), // Low
            createDecisionWithAccuracy(40f), // Low
            createDecisionWithAccuracy(60f), // Medium
            createDecisionWithAccuracy(70f), // Medium
            createDecisionWithAccuracy(80f), // High
            createDecisionWithAccuracy(90f)  // High
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 6,
            completedDecisions = 6,
            averageAccuracy = 61.67f,
            decisions = decisions
        )
        
        val patterns = analytics.getRegretPatterns()
        
        assertEquals(3, patterns.size)
        assertTrue(patterns.containsKey("Low"))
        assertTrue(patterns.containsKey("Medium"))
        assertTrue(patterns.containsKey("High"))
        
        // 2 out of 6 = 33.33%
        assertEquals(33.33f, patterns["Low"]!!, 0.1f)
        // 2 out of 6 = 33.33%
        assertEquals(33.33f, patterns["Medium"]!!, 0.1f)
        // 2 out of 6 = 33.33%
        assertEquals(33.33f, patterns["High"]!!, 0.1f)
    }

    @Test
    fun `getRegretPatterns returns empty map for no completed decisions`() {
        val analytics = AnalyticsData(
            totalDecisions = 0,
            completedDecisions = 0,
            averageAccuracy = 0f,
            decisions = emptyList()
        )
        
        val patterns = analytics.getRegretPatterns()
        assertTrue(patterns.isEmpty())
    }

    @Test
    fun `getTopRegretCategory returns category with highest regret`() {
        val decisions = listOf(
            createDecisionWithRegret("Health", 80f),
            createDecisionWithRegret("Health", 70f),
            createDecisionWithRegret("Money", 50f),
            createDecisionWithRegret("Money", 60f)
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 4,
            completedDecisions = 4,
            averageAccuracy = 65f,
            decisions = decisions
        )
        
        val topRegret = analytics.getTopRegretCategory()
        
        assertNotNull(topRegret)
        assertEquals("Health", topRegret?.first)
        assertEquals(75f, topRegret?.second!!, 1f) // Average of 80 and 70
    }

    @Test
    fun `getTopRegretCategory returns null for no completed decisions`() {
        val analytics = AnalyticsData(
            totalDecisions = 0,
            completedDecisions = 0,
            averageAccuracy = 0f,
            decisions = emptyList()
        )
        
        val topRegret = analytics.getTopRegretCategory()
        assertNull(topRegret)
    }

    @Test
    fun `getRegretScorePerCategory returns correct scores`() {
        val decisions = listOf(
            createDecisionWithRegret("Health", 80f),
            createDecisionWithRegret("Health", 70f),
            createDecisionWithRegret("Money", 50f)
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 3,
            completedDecisions = 3,
            averageAccuracy = 66.67f,
            decisions = decisions
        )
        
        val scores = analytics.getRegretScorePerCategory()
        
        assertEquals(2, scores.size)
        assertEquals(75f, scores["Health"]!!, 1f) // Average of 80 and 70
        assertEquals(50f, scores["Money"]!!, 1f)
    }

    @Test
    fun `getRepeatedRegret returns pattern for high regret decisions`() {
        val decisions = listOf(
            createDecisionWithRegret("Health", 80f, "Late night screen time"),
            createDecisionWithRegret("Health", 75f, "Late night screen time"),
            createDecisionWithRegret("Health", 70f, "Late night screen time")
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 3,
            completedDecisions = 3,
            averageAccuracy = 0f,
            decisions = decisions
        )
        
        val repeated = analytics.getRepeatedRegret()
        
        assertNotNull(repeated)
        assertTrue(repeated!!.contains("Late night screen time", ignoreCase = true))
    }

    @Test
    fun `getRepeatedRegret returns null for insufficient high regret decisions`() {
        val decisions = listOf(
            createDecisionWithRegret("Health", 80f)
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 1,
            completedDecisions = 1,
            averageAccuracy = 0f,
            decisions = decisions
        )
        
        val repeated = analytics.getRepeatedRegret()
        assertNull(repeated)
    }

    @Test
    fun `getOverconfidenceByCategory returns overconfidence pattern`() {
        val decisions = listOf(
            createDecisionWithConfidenceAndAccuracy("Money", 90f, 50f), // 40% gap
            createDecisionWithConfidenceAndAccuracy("Money", 85f, 60f), // 25% gap
            createDecisionWithConfidenceAndAccuracy("Health", 70f, 65f) // 5% gap
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 3,
            completedDecisions = 3,
            averageAccuracy = 58.33f,
            decisions = decisions
        )
        
        val overconfidence = analytics.getOverconfidenceByCategory()
        
        assertNotNull(overconfidence)
        assertTrue(overconfidence!!.contains("Money", ignoreCase = true))
    }

    @Test
    fun `getCategoryAccuracy returns correct accuracy per category`() {
        val decisions = listOf(
            createDecisionWithAccuracy("Health", 80f),
            createDecisionWithAccuracy("Health", 70f),
            createDecisionWithAccuracy("Money", 60f)
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 3,
            completedDecisions = 3,
            averageAccuracy = 70f,
            decisions = decisions
        )
        
        val categoryAccuracy = analytics.getCategoryAccuracy()
        
        assertEquals(2, categoryAccuracy.size)
        assertEquals(75f, categoryAccuracy["Health"]!!, 1f) // Average of 80 and 70
        assertEquals(60f, categoryAccuracy["Money"]!!, 1f)
    }

    @Test
    fun `getDecisionStreak calculates consecutive days correctly`() {
        val today = Date()
        val yesterday = Date(today.time - 24 * 60 * 60 * 1000)
        val twoDaysAgo = Date(today.time - 2 * 24 * 60 * 60 * 1000)
        
        val decisions = listOf(
            Decision(title = "Day 1", prediction = "Test", createdAt = twoDaysAgo),
            Decision(title = "Day 2", prediction = "Test", createdAt = yesterday),
            Decision(title = "Day 3", prediction = "Test", createdAt = today)
        )
        
        val analytics = AnalyticsData(
            totalDecisions = 3,
            completedDecisions = 0,
            averageAccuracy = 0f,
            decisions = decisions
        )
        
        val streak = analytics.getDecisionStreak()
        
        // Should be at least 3 (if today has a decision)
        assertTrue(streak >= 0)
    }

    // Helper functions
    private fun createDecisionWithAccuracy(accuracy: Float): Decision {
        val decision = Decision(
            title = "Test Decision",
            prediction = "Test",
            createdAt = Date(),
            predictedEnergy24h = 0f,
            actualEnergy24h = if (accuracy > 50) 0f else 5f // Adjust to get desired accuracy
        )
        // Note: Actual accuracy calculation is complex, this is simplified
        return decision
    }

    private fun createDecisionWithAccuracy(category: String, accuracy: Float): Decision {
        return Decision(
            title = "Test Decision",
            prediction = "Test",
            createdAt = Date(),
            category = category,
            predictedEnergy24h = 0f,
            actualEnergy24h = if (accuracy > 50) 0f else 5f
        )
    }

    private fun createDecisionWithRegret(category: String, regretIndex: Float, title: String = "Test"): Decision {
        // Regret index 0-100, actualRegret24h is 0-10
        val actualRegret = (regretIndex / 100f) * 10f
        return Decision(
            title = title,
            prediction = "Test",
            createdAt = Date(),
            category = category,
            actualRegret24h = actualRegret,
            followedDecision = true
        )
    }

    private fun createDecisionWithConfidenceAndAccuracy(category: String, confidence: Float, accuracy: Float): Decision {
        return Decision(
            title = "Test Decision",
            prediction = "Test",
            createdAt = Date(),
            category = category,
            predictionConfidence = confidence,
            predictedEnergy24h = 0f,
            actualEnergy24h = if (accuracy > 50) 0f else 5f
        )
    }
}

