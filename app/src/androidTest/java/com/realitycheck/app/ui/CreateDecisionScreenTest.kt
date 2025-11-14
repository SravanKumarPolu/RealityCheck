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

        composeTestRule.onNodeWithText("What are you deciding?")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun createDecisionScreen_showsCategoryChips() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Check for category chips
        composeTestRule.onNodeWithText("Health")
            .assertExists()
        
        composeTestRule.onNodeWithText("Work")
            .assertExists()
        
        composeTestRule.onNodeWithText("Money")
            .assertExists()
    }

    @Test
    fun createDecisionScreen_showsPredictionSliders() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Scroll to find sliders
        composeTestRule.onRoot().performTouchInput {
            swipeUp()
        }

        // Check for slider labels
        composeTestRule.onNodeWithText("Energy", substring = true, useUnmergedTree = true)
            .assertExists()
        
        composeTestRule.onNodeWithText("Mood", substring = true, useUnmergedTree = true)
            .assertExists()
    }
    
    @Test
    fun createDecisionScreen_showsLockInButton() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Lock in prediction")
            .assertExists()
            .assertIsDisplayed()
    }
    
    @Test
    fun createDecisionScreen_showsTemplates() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Check for template section
        composeTestRule.onNodeWithText("Quick Start Templates", substring = true)
            .assertExists()
    }

    @Test
    fun createDecisionScreen_categorySelectionWorks() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Click on Health category
        composeTestRule.onNodeWithText("Health")
            .performClick()
            .assertIsSelected()
    }

    @Test
    fun createDecisionScreen_canEnterTitle() {
        composeTestRule.setContent {
            CreateDecisionScreen(
                onNavigateBack = {}
            )
        }

        // Find title field and enter text
        composeTestRule.onNodeWithText("What are you deciding?")
            .performTextInput("Should I order food?")

        // Verify text was entered
        composeTestRule.onNodeWithText("Should I order food?", substring = true)
            .assertExists()
    }
}
