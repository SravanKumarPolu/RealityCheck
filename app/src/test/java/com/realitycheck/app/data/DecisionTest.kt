package com.realitycheck.app.data

import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class DecisionTest {

    @Test
    fun `isCompleted returns true when outcome is not null`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            outcome = "Test outcome",
            createdAt = Date()
        )
        assertTrue(decision.isCompleted())
    }

    @Test
    fun `isCompleted returns true when actualEnergy24h is not null`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            actualEnergy24h = 3.0f,
            createdAt = Date()
        )
        assertTrue(decision.isCompleted())
    }

    @Test
    fun `isCompleted returns false when neither outcome nor actualEnergy24h is set`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        assertFalse(decision.isCompleted())
    }

    @Test
    fun `getAccuracy returns null when no quantitative data available and no outcome`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        assertNull(decision.getAccuracy())
    }

    @Test
    fun `getAccuracy calculates correct percentage for perfect match`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            predictedEnergy24h = 3.0f,
            predictedMood24h = 2.0f,
            predictedStress24h = -1.0f,
            actualEnergy24h = 3.0f,
            actualMood24h = 2.0f,
            actualStress24h = -1.0f
        )
        val accuracy = decision.getAccuracy()
        assertNotNull(accuracy)
        assertEquals(100f, accuracy!!, 0.1f)
    }

    @Test
    fun `getAccuracy calculates correct percentage for partial match`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            predictedEnergy24h = 3.0f,
            predictedMood24h = 2.0f,
            actualEnergy24h = 1.0f, // Error of 2
            actualMood24h = 0.0f    // Error of 2
        )
        val accuracy = decision.getAccuracy()
        assertNotNull(accuracy)
        // Average error = 2, max error = 10, accuracy = 100 - (2/10)*100 = 80%
        assertEquals(80f, accuracy!!, 1.0f)
    }

    @Test
    fun `getAccuracy handles regret normalization correctly`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            predictedRegretChance24h = 0f, // -5 to +5 scale, 0 = middle
            actualRegret24h = 5f           // 0-10 scale, 5 = middle
        )
        val accuracy = decision.getAccuracy()
        assertNotNull(accuracy)
        // Predicted normalized = 0 + 5 = 5, Actual = 5, Error = 0, Accuracy = 100%
        assertEquals(100f, accuracy!!, 0.1f)
    }

    @Test
    fun `getAccuracy falls back to text-based when quantitative not available`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "I will feel happy and energetic",
            outcome = "I felt happy and energetic today",
            createdAt = Date()
        )
        val accuracy = decision.getAccuracy()
        assertNotNull(accuracy)
        assertTrue(accuracy!! > 0f)
    }

    @Test
    fun `getRegretIndex returns null when actualRegret24h is null`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        assertNull(decision.getRegretIndex())
    }

    @Test
    fun `getRegretIndex calculates correctly for low regret`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            actualRegret24h = 2.0f,
            followedDecision = true
        )
        val regretIndex = decision.getRegretIndex()
        assertNotNull(regretIndex)
        // Regret score = 2, normalized to 0-100: (2/13)*100 = 15.38%
        assertTrue(regretIndex!! < 20f)
    }

    @Test
    fun `getRegretIndex adds penalty for not following decision`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            actualRegret24h = 3.0f,
            followedDecision = false // Adds 2 points
        )
        val regretIndex = decision.getRegretIndex()
        assertNotNull(regretIndex)
        // Regret score = 3 + 2 = 5, normalized: (5/13)*100 = 38.46%
        assertTrue(regretIndex!! > 35f && regretIndex < 40f)
    }

    @Test
    fun `getRegretIndex adds extra point for high regret even when followed`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            actualRegret24h = 8.0f, // High regret
            followedDecision = true  // Still adds 1 point
        )
        val regretIndex = decision.getRegretIndex()
        assertNotNull(regretIndex)
        // Regret score = 8 + 1 = 9, normalized: (9/13)*100 = 69.23%
        assertTrue(regretIndex!! > 65f && regretIndex < 70f)
    }

    @Test
    fun `getRegretIndex clamps to 0-100 range`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            actualRegret24h = 10.0f,
            followedDecision = false // Adds 2
        )
        val regretIndex = decision.getRegretIndex()
        assertNotNull(regretIndex)
        // Should be clamped to 100
        assertTrue(regretIndex!! <= 100f)
    }

    @Test
    fun `getHoursUntilCheckIn returns null when reminderDate is null`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        assertNull(decision.getHoursUntilCheckIn())
    }

    @Test
    fun `getHoursUntilCheckIn returns null when decision is completed`() {
        val futureDate = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            outcome = "Completed",
            reminderDate = futureDate,
            createdAt = Date()
        )
        assertNull(decision.getHoursUntilCheckIn())
    }

    @Test
    fun `getHoursUntilCheckIn calculates correct hours`() {
        val futureTime = System.currentTimeMillis() + (48 * 60 * 60 * 1000L) // 48 hours
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDate = Date(futureTime),
            createdAt = Date()
        )
        val hours = decision.getHoursUntilCheckIn()
        assertNotNull(hours)
        assertTrue(hours!! >= 47 && hours <= 49) // Allow 1 hour tolerance
    }

    @Test
    fun `getCheckInTimeString returns overdue when hours is 0 or negative`() {
        val pastDate = Date(System.currentTimeMillis() - 1000L)
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDate = pastDate,
            createdAt = Date()
        )
        val timeString = decision.getCheckInTimeString()
        assertNotNull(timeString)
        assertEquals("Check-in overdue", timeString)
    }

    @Test
    fun `getRegretIndicator returns null for incomplete decisions`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        assertNull(decision.getRegretIndicator())
    }

    @Test
    fun `getRegretIndicator returns checkmark for low regret`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            actualRegret24h = 3.0f, // Low regret
            followedDecision = true
        )
        val indicator = decision.getRegretIndicator()
        assertEquals("✅", indicator)
    }

    @Test
    fun `getRegretIndicator returns emoji for high regret`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            actualRegret24h = 8.0f, // High regret
            followedDecision = true
        )
        val indicator = decision.getRegretIndicator()
        assertEquals("😬", indicator)
    }

    @Test
    fun `getDisplayCategory returns category when set`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            category = "Health",
            createdAt = Date()
        )
        assertEquals("Health", decision.getDisplayCategory())
    }

    @Test
    fun `getDisplayCategory returns Other when category is null`() {
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        assertEquals("Other", decision.getDisplayCategory())
    }

    @Test
    fun `getDefaultCheckInDays returns correct defaults for each category`() {
        assertEquals(1, Decision.getDefaultCheckInDays("Health"))
        assertEquals(7, Decision.getDefaultCheckInDays("Money"))
        assertEquals(3, Decision.getDefaultCheckInDays("Work"))
        assertEquals(3, Decision.getDefaultCheckInDays("Study"))
        assertEquals(7, Decision.getDefaultCheckInDays("Relationships"))
        assertEquals(7, Decision.getDefaultCheckInDays("Habits"))
        assertEquals(3, Decision.getDefaultCheckInDays("Other"))
        assertEquals(3, Decision.getDefaultCheckInDays(null))
    }
}

