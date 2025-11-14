package com.realitycheck.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
class DecisionRepositoryTest {

    private lateinit var repository: DecisionRepository
    private lateinit var database: DecisionDatabase
    private lateinit var dao: DecisionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            DecisionDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.decisionDao()
        repository = DecisionRepository(dao)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `getAllDecisions returns flow of all decisions`() = runTest {
        // Given
        val decision1 = Decision(
            title = "Decision 1",
            prediction = "Prediction 1",
            createdAt = Date(),
            category = "Health"
        )
        val decision2 = Decision(
            title = "Decision 2",
            prediction = "Prediction 2",
            createdAt = Date(),
            category = "Work"
        )
        dao.insertDecision(decision1)
        dao.insertDecision(decision2)

        // When
        val decisions = repository.getAllDecisions().first()

        // Then
        assertEquals(2, decisions.size)
        assertTrue(decisions.any { it.title == "Decision 1" })
        assertTrue(decisions.any { it.title == "Decision 2" })
    }

    @Test
    fun `getAllDecisions returns empty list when no decisions exist`() = runTest {
        // When
        val decisions = repository.getAllDecisions().first()

        // Then
        assertTrue(decisions.isEmpty())
    }

    @Test
    fun `insertDecision returns valid ID`() = runTest {
        // Given
        val decision = Decision(
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            category = "Health"
        )
        
        // When
        val id = repository.insertDecision(decision)

        // Then
        assertTrue(id > 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `insertDecision throws exception for blank title`() = runTest {
        val decision = Decision(
            title = "",
            prediction = "Test",
            createdAt = Date()
        )
        
        repository.insertDecision(decision)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `insertDecision throws exception for whitespace only title`() = runTest {
        val decision = Decision(
            title = "   ",
            prediction = "Test",
            createdAt = Date()
        )

        repository.insertDecision(decision)
    }

    @Test
    fun `getDecisionById returns correct decision`() = runTest {
        // Given
        val decision = Decision(
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date(),
            category = "Health"
        )
        val id = repository.insertDecision(decision)

        // When
        val retrieved = repository.getDecisionById(id)

        // Then
        assertNotNull(retrieved)
        assertEquals(decision.title, retrieved?.title)
        assertEquals(decision.prediction, retrieved?.prediction)
        assertEquals(decision.category, retrieved?.category)
    }

    @Test
    fun `getDecisionById returns null for invalid ID`() = runTest {
        // When
        val decision = repository.getDecisionById(-1L)

        // Then
        assertNull(decision)
    }

    @Test
    fun `getDecisionById returns null for non-existent ID`() = runTest {
        // When
        val decision = repository.getDecisionById(999L)

        // Then
        assertNull(decision)
    }

    @Test
    fun `updateDecision updates existing decision`() = runTest {
        // Given
        val decision = Decision(
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        val id = repository.insertDecision(decision)

        // When
        val updated = decision.copy(
            id = id,
            outcome = "Updated outcome",
            actualEnergy24h = 3.0f
        )
        repository.updateDecision(updated)

        // Then
        val retrieved = repository.getDecisionById(id)
        assertNotNull(retrieved)
        assertEquals("Updated outcome", retrieved?.outcome)
        assertEquals(3.0f, retrieved?.actualEnergy24h)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `updateDecision throws exception for invalid ID`() = runTest {
        val decision = Decision(
            id = 0L,
            title = "Test",
            prediction = "Test",
            createdAt = Date()
        )
        
        repository.updateDecision(decision)
    }

    @Test
    fun `deleteDecision removes decision`() = runTest {
        // Given
        val decision = Decision(
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        val id = repository.insertDecision(decision)

        // When
        repository.deleteDecision(decision.copy(id = id))

        // Then
        val retrieved = repository.getDecisionById(id)
        assertNull(retrieved)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `deleteDecision throws exception for invalid ID`() = runTest {
        val decision = Decision(
            id = 0L,
            title = "Test",
            prediction = "Test",
            createdAt = Date()
        )
        
        repository.deleteDecision(decision)
    }

    @Test
    fun `getCompletedDecisions returns only completed decisions`() = runTest {
        // Given
        val completed1 = Decision(
            title = "Completed 1",
            prediction = "Prediction 1",
            createdAt = Date(),
            outcome = "Outcome 1"
        )
        val completed2 = Decision(
            title = "Completed 2",
            prediction = "Prediction 2",
            createdAt = Date(),
            actualEnergy24h = 3.0f
        )
        val active = Decision(
            title = "Active",
            prediction = "Prediction",
            createdAt = Date()
        )

        repository.insertDecision(completed1)
        repository.insertDecision(completed2)
        repository.insertDecision(active)

        // When
        val completed = repository.getCompletedDecisions().first()

        // Then
        assertEquals(2, completed.size)
        assertTrue(completed.all { it.isCompleted() })
        assertTrue(completed.any { it.title == "Completed 1" })
        assertTrue(completed.any { it.title == "Completed 2" })
        assertFalse(completed.any { it.title == "Active" })
    }

    @Test
    fun `getCompletionRate calculates correct rate`() = runTest {
        // Given
        val completed = Decision(
            title = "Completed",
            prediction = "Prediction",
            createdAt = Date(),
            outcome = "Outcome"
        )
        val active1 = Decision(
            title = "Active 1",
            prediction = "Prediction",
            createdAt = Date()
        )
        val active2 = Decision(
            title = "Active 2",
            prediction = "Prediction",
            createdAt = Date()
        )

        repository.insertDecision(completed)
        repository.insertDecision(active1)
        repository.insertDecision(active2)

        // When
        val rate = repository.getCompletionRate().first()

        // Then
        assertTrue(rate >= 0f && rate <= 100f)
        // Should be approximately 33.33% (1 out of 3)
        assertTrue(rate >= 30f && rate <= 40f)
    }

    @Test
    fun `getDecisionsByCategory returns only decisions in category`() = runTest {
        // Given
        val health1 = Decision(
            title = "Health 1",
            prediction = "Prediction",
            createdAt = Date(),
            category = "Health"
        )
        val health2 = Decision(
            title = "Health 2",
            prediction = "Prediction",
            createdAt = Date(),
            category = "Health"
        )
        val work = Decision(
            title = "Work",
            prediction = "Prediction",
            createdAt = Date(),
            category = "Work"
        )

        repository.insertDecision(health1)
        repository.insertDecision(health2)
        repository.insertDecision(work)

        // When
        val healthDecisions = repository.getDecisionsByCategory("Health").first()

        // Then
        assertEquals(2, healthDecisions.size)
        assertTrue(healthDecisions.all { it.category == "Health" })
    }

    @Test
    fun `getAllCategories returns unique categories`() = runTest {
        // Given
        val decision1 = Decision(
            title = "Decision 1",
            prediction = "Prediction",
            createdAt = Date(),
            category = "Health"
        )
        val decision2 = Decision(
            title = "Decision 2",
            prediction = "Prediction",
            createdAt = Date(),
            category = "Health"
        )
        val decision3 = Decision(
            title = "Decision 3",
            prediction = "Prediction",
            createdAt = Date(),
            category = "Work"
        )

        repository.insertDecision(decision1)
        repository.insertDecision(decision2)
        repository.insertDecision(decision3)

        // When
        val categories = repository.getAllCategories()

        // Then
        assertEquals(2, categories.size)
        assertTrue(categories.contains("Health"))
        assertTrue(categories.contains("Work"))
    }

    @Test
    fun `getAllTags returns unique tags from decisions`() = runTest {
        // Given
        val decision1 = Decision(
            title = "Decision 1",
            prediction = "Prediction",
            createdAt = Date(),
            tags = listOf("tag1", "tag2")
        )
        val decision2 = Decision(
            title = "Decision 2",
            prediction = "Prediction",
            createdAt = Date(),
            tags = listOf("tag2", "tag3")
        )

        repository.insertDecision(decision1)
        repository.insertDecision(decision2)

        // When
        val tags = repository.getAllTags()

        // Then
        assertEquals(3, tags.size)
        assertTrue(tags.contains("tag1"))
        assertTrue(tags.contains("tag2"))
        assertTrue(tags.contains("tag3"))
    }

    @Test
    fun `filterDecisions filters by category`() = runTest {
        // Given
        val decisions = listOf(
            Decision(
                id = 1,
                title = "Health Decision",
                prediction = "Prediction",
                createdAt = Date(),
                category = "Health"
            ),
            Decision(
                id = 2,
                title = "Work Decision",
                prediction = "Prediction",
                createdAt = Date(),
                category = "Work"
            )
        )

        // When
        val filtered = repository.filterDecisions(
            decisions = decisions,
            selectedCategory = "Health",
            selectedTags = emptyList()
        )

        // Then
        assertEquals(1, filtered.size)
        assertEquals("Health Decision", filtered.first().title)
    }

    @Test
    fun `filterDecisions filters by tags`() = runTest {
        // Given
        val decisions = listOf(
            Decision(
                id = 1,
                title = "Decision 1",
                prediction = "Prediction",
                createdAt = Date(),
                tags = listOf("important", "urgent")
            ),
            Decision(
                id = 2,
                title = "Decision 2",
                prediction = "Prediction",
                createdAt = Date(),
                tags = listOf("casual")
            )
        )

        // When
        val filtered = repository.filterDecisions(
            decisions = decisions,
            selectedCategory = null,
            selectedTags = listOf("important")
        )

        // Then
        assertEquals(1, filtered.size)
        assertEquals("Decision 1", filtered.first().title)
    }

    @Test
    fun `filterDecisions returns all when no filters applied`() = runTest {
        // Given
        val decisions = listOf(
            Decision(
                id = 1,
                title = "Decision 1",
                prediction = "Prediction",
                createdAt = Date(),
                category = "Health"
            ),
            Decision(
                id = 2,
                title = "Decision 2",
                prediction = "Prediction",
                createdAt = Date(),
                category = "Work"
            )
        )

        // When
        val filtered = repository.filterDecisions(
            decisions = decisions,
            selectedCategory = null,
            selectedTags = emptyList()
        )

        // Then
        assertEquals(2, filtered.size)
    }
}

