package com.realitycheck.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.hilt.android.testing.HiltAndroidRule
import androidx.hilt.android.testing.HiltAndroidTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.realitycheck.app.ui.screens.CreateDecisionScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CreateDecisionScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun createDecisionScreen_showsTitleField() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Verify screen title
        composeTestRule.onNodeWithText("New Decision", substring = true)
            .assertExists()
    }

    @Test
    fun createDecisionScreen_showsCategoryChips() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Categories should be visible
        composeTestRule.onNodeWithText("Health", substring = true)
            .assertExists()
    }

    @Test
    fun createDecisionScreen_showsPredictionSliders() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Verify sliders are present by checking for labels
        composeTestRule.onNodeWithText("Energy", substring = true, ignoreCase = true)
            .assertExists()
    }
    
    @Test
    fun createDecisionScreen_showsTagsSection() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Tags section should be visible
        composeTestRule.onNodeWithText("Tags", substring = true, ignoreCase = true)
            .assertExists()
    }
    
    @Test
    fun createDecisionScreen_showsTemplates() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Templates section should be visible
        composeTestRule.onNodeWithText("Quick Start Templates", substring = true, ignoreCase = true)
            .assertExists()
    }
}

