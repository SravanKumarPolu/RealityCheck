package com.realitycheck.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.hilt.android.testing.HiltAndroidRule
import androidx.hilt.android.testing.HiltAndroidTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.realitycheck.app.data.Decision
import com.realitycheck.app.ui.screens.RealityCheckScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class RealityCheckScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun realityCheckScreen_showsDecisionTitle() {
        composeTestRule.setContent {
            RealityCheckScreen(
                decisionId = 1L,
                onNavigateBack = {}
            )
        }

        // Wait for loading to complete
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Reality Check").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Reality Check")
            .assertExists()
    }

    @Test
    fun realityCheckScreen_showsOutcomeSliders() {
        composeTestRule.setContent {
            RealityCheckScreen(
                decisionId = 1L,
                onNavigateBack = {}
            )
        }

        // Wait for loading
        composeTestRule.waitForIdle()

        // Check for slider labels
        composeTestRule.onNodeWithText("What happened actually?", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun realityCheckScreen_showsFollowDecisionToggle() {
        composeTestRule.setContent {
            RealityCheckScreen(
                decisionId = 1L,
                onNavigateBack = {}
            )
        }

        // Wait for loading
        composeTestRule.waitForIdle()

        // Check for follow decision question
        composeTestRule.onNodeWithText("Did you follow the decision?", substring = true, useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun realityCheckScreen_showsSaveRealityButton() {
        composeTestRule.setContent {
            RealityCheckScreen(
                decisionId = 1L,
                onNavigateBack = {}
            )
        }

        // Wait for loading
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Save Reality")
            .assertExists()
    }

    @Test
    fun realityCheckScreen_followDecisionButtonsWork() {
        composeTestRule.setContent {
            RealityCheckScreen(
                decisionId = 1L,
                onNavigateBack = {}
            )
        }

        // Wait for loading
        composeTestRule.waitForIdle()

        // Find and click "Yes" button
        composeTestRule.onNodeWithText("Yes, I did it", substring = true)
            .assertExists()
            .performClick()
    }
}

