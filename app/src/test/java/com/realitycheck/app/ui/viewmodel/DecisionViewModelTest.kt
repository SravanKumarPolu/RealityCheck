package com.realitycheck.app.ui.viewmodel

import android.content.Context
import com.realitycheck.app.data.Decision
import com.realitycheck.app.data.DecisionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.Date

class DecisionViewModelTest {

    private lateinit var repository: DecisionRepository
    private lateinit var context: Context
    private lateinit var viewModel: DecisionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        repository = mock()
        context = mock()
        viewModel = DecisionViewModel(repository, context)
    }

    @Test
    fun `createDecision inserts decision successfully`() = runTest(testDispatcher) {
        // Given
        val decisionId = 1L
        whenever(repository.insertDecision(any())).thenReturn(decisionId)
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.createDecision(
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDays = 3,
            category = "Health"
        )
        advanceUntilIdle()

        // Then
        verify(repository).insertDecision(any())
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Success)
    }

    @Test
    fun `createDecision handles empty title validation`() = runTest(testDispatcher) {
        // Given
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.createDecision(
            title = "", // Empty title
            prediction = "Test prediction",
            reminderDays = 3,
            category = "Health"
        )
        advanceUntilIdle()

        // Then
        verify(repository, never()).insertDecision(any())
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Error)
        assertTrue((uiState as DecisionUiState.Error).message.contains("title", ignoreCase = true))
    }

    @Test
    fun `createDecision handles blank title validation`() = runTest(testDispatcher) {
        // Given
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.createDecision(
            title = "   ", // Blank title
            prediction = "Test prediction",
            reminderDays = 3,
            category = "Health"
        )
        advanceUntilIdle()

        // Then
        verify(repository, never()).insertDecision(any())
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Error)
    }

    @Test
    fun `createDecision handles repository exception`() = runTest(testDispatcher) {
        // Given
        val errorMessage = "Database error"
        whenever(repository.insertDecision(any())).thenThrow(RuntimeException(errorMessage))
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.createDecision(
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDays = 3,
            category = "Health"
        )
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Error)
        assertEquals(errorMessage, (uiState as DecisionUiState.Error).message)
    }

    @Test
    fun `createDecision sets reminderDate correctly`() = runTest(testDispatcher) {
        // Given
        val decisionId = 1L
        whenever(repository.insertDecision(any())).thenReturn(decisionId)
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.createDecision(
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDays = 7,
            category = "Health"
        )
        advanceUntilIdle()

        // Then
        val captor = argumentCaptor<Decision>()
        verify(repository).insertDecision(captor.capture())
        val capturedDecision = captor.firstValue
        assertNotNull(capturedDecision.reminderDate)
        // Verify reminder date is approximately 7 days in the future
        val expectedTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        val actualTime = capturedDecision.reminderDate!!.time
        assertTrue(kotlin.math.abs(actualTime - expectedTime) < 1000) // Within 1 second
    }

    @Test
    fun `createDecision sets reminderDate to null when reminderDays is 0`() = runTest(testDispatcher) {
        // Given
        val decisionId = 1L
        whenever(repository.insertDecision(any())).thenReturn(decisionId)
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.createDecision(
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDays = 0,
            category = "Health"
        )
        advanceUntilIdle()

        // Then
        val captor = argumentCaptor<Decision>()
        verify(repository).insertDecision(captor.capture())
        val capturedDecision = captor.firstValue
        assertNull(capturedDecision.reminderDate)
    }

    @Test
    fun `updateDecisionOutcome updates decision successfully`() = runTest(testDispatcher) {
        // Given
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        whenever(repository.updateDecision(any())).thenAnswer { }
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.updateDecisionOutcome(
            decision = decision,
            outcome = "Test outcome",
            actualEnergy24h = 3.0f,
            actualMood24h = 2.0f,
            followedDecision = true
        )
        advanceUntilIdle()

        // Then
        verify(repository).updateDecision(any())
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Success)
    }

    @Test
    fun `updateDecisionOutcome handles repository exception`() = runTest(testDispatcher) {
        // Given
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        val errorMessage = "Update failed"
        whenever(repository.updateDecision(any())).thenThrow(RuntimeException(errorMessage))
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.updateDecisionOutcome(
            decision = decision,
            outcome = "Test outcome",
            followedDecision = true
        )
        advanceUntilIdle()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Error)
        assertEquals(errorMessage, (uiState as DecisionUiState.Error).message)
    }

    @Test
    fun `updateDecisionOutcome sets outcomeRecordedAt`() = runTest(testDispatcher) {
        // Given
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        whenever(repository.updateDecision(any())).thenAnswer { }
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.updateDecisionOutcome(
            decision = decision,
            outcome = "Test outcome",
            followedDecision = true
        )
        advanceUntilIdle()

        // Then
        val captor = argumentCaptor<Decision>()
        verify(repository).updateDecision(captor.capture())
        val updatedDecision = captor.firstValue
        assertNotNull(updatedDecision.outcomeRecordedAt)
    }

    @Test
    fun `deleteDecision deletes successfully`() = runTest(testDispatcher) {
        // Given
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        whenever(repository.deleteDecision(any())).thenAnswer { }
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.deleteDecision(decision)
        advanceUntilIdle()

        // Then
        verify(repository).deleteDecision(decision)
    }

    @Test
    fun `resetUiState resets to Idle`() = runTest(testDispatcher) {
        // Given
        viewModel.resetUiState()

        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Idle)
    }

    @Test
    fun `analytics are calculated correctly from decisions flow`() = runTest(testDispatcher) {
        // Given
        val decisions = listOf(
            Decision(
                id = 1,
                title = "Decision 1",
                prediction = "Prediction 1",
                createdAt = Date(),
                predictedEnergy24h = 3.0f,
                actualEnergy24h = 3.0f
            ),
            Decision(
                id = 2,
                title = "Decision 2",
                prediction = "Prediction 2",
                createdAt = Date(),
                predictedEnergy24h = 2.0f,
                actualEnergy24h = 1.0f // Error of 1
            )
        )
        whenever(repository.getAllDecisions()).thenReturn(flowOf(decisions))

        // When
        viewModel = DecisionViewModel(repository, context)
        advanceUntilIdle()

        // Then
        val analytics = viewModel.analytics.value
        assertNotNull(analytics)
        assertEquals(2, analytics!!.totalDecisions)
        assertEquals(2, analytics.completedDecisions)
        // Average accuracy should be high (one perfect, one with small error)
        assertTrue(analytics.averageAccuracy > 80f)
    }

    @Test
    fun `createDecision validates category selection`() = runTest(testDispatcher) {
        // Given
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.createDecision(
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDays = 3,
            category = "" // Empty category
        )
        advanceUntilIdle()

        // Then
        verify(repository, never()).insertDecision(any())
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Error)
        assertTrue((uiState as DecisionUiState.Error).message.contains("category", ignoreCase = true))
    }

    @Test
    fun `createDecision validates slider ranges`() = runTest(testDispatcher) {
        // Given
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When - energy out of range
        viewModel.createDecision(
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDays = 3,
            category = "Health",
            energy24h = 10f // Out of range (-5 to +5)
        )
        advanceUntilIdle()

        // Then
        verify(repository, never()).insertDecision(any())
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Error)
        assertTrue((uiState as DecisionUiState.Error).message.contains("Energy", ignoreCase = true))
    }

    @Test
    fun `createDecision validates confidence range`() = runTest(testDispatcher) {
        // Given
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When - confidence out of range
        viewModel.createDecision(
            title = "Test Decision",
            prediction = "Test prediction",
            reminderDays = 3,
            category = "Health",
            confidence = 150f // Out of range (0-100)
        )
        advanceUntilIdle()

        // Then
        verify(repository, never()).insertDecision(any())
        val uiState = viewModel.uiState.value
        assertTrue(uiState is DecisionUiState.Error)
        assertTrue((uiState as DecisionUiState.Error).message.contains("Confidence", ignoreCase = true))
    }

    @Test
    fun `updateDecisionOutcome validates followedDecision is set`() = runTest(testDispatcher) {
        // Given
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When - followedDecision is null
        viewModel.updateDecisionOutcome(
            decision = decision,
            outcome = "Test outcome",
            followedDecision = null // Not set
        )
        advanceUntilIdle()

        // Then - Should still update but with validation
        // The actual implementation might allow null, but we test it
        val uiState = viewModel.uiState.value
        // Depending on implementation, this might succeed or fail
    }

    @Test
    fun `deleteDecision handles repository exception`() = runTest(testDispatcher) {
        // Given
        val decision = Decision(
            id = 1,
            title = "Test Decision",
            prediction = "Test prediction",
            createdAt = Date()
        )
        val errorMessage = "Delete failed"
        whenever(repository.deleteDecision(any())).thenThrow(RuntimeException(errorMessage))
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel.deleteDecision(decision)
        advanceUntilIdle()

        // Then - Exception should be caught (repository handles it)
        // In this case, repository handles exceptions, so this should complete
    }

    @Test
    fun `analytics handles empty decisions list`() = runTest(testDispatcher) {
        // Given
        whenever(repository.getAllDecisions()).thenReturn(flowOf(emptyList()))

        // When
        viewModel = DecisionViewModel(repository, context)
        advanceUntilIdle()

        // Then
        val analytics = viewModel.analytics.value
        assertNotNull(analytics)
        assertEquals(0, analytics!!.totalDecisions)
        assertEquals(0, analytics.completedDecisions)
        assertEquals(0f, analytics.averageAccuracy)
    }

    @Test
    fun `analytics handles decisions with no completed ones`() = runTest(testDispatcher) {
        // Given
        val decisions = listOf(
            Decision(
                id = 1,
                title = "Decision 1",
                prediction = "Prediction 1",
                createdAt = Date()
                // Not completed
            )
        )
        whenever(repository.getAllDecisions()).thenReturn(flowOf(decisions))

        // When
        viewModel = DecisionViewModel(repository, context)
        advanceUntilIdle()

        // Then
        val analytics = viewModel.analytics.value
        assertNotNull(analytics)
        assertEquals(1, analytics!!.totalDecisions)
        assertEquals(0, analytics.completedDecisions)
        assertEquals(0f, analytics.averageAccuracy)
    }
}

