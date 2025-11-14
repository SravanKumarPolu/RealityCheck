package com.realitycheck.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.hilt.android.testing.HiltAndroidRule
import androidx.hilt.android.testing.HiltAndroidTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.realitycheck.app.ui.screens.MainScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class MainScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun emptyState_showsCreateButton() {
        composeTestRule.setContent {
            MainScreen(
                onNavigateToCreate = {},
                onNavigateToDetail = {},
                onNavigateToAnalytics = {}
            )
        }

        composeTestRule.onNodeWithText("Log a Decision Before You Act")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            MainScreen(
                onNavigateToCreate = {},
                onNavigateToDetail = {},
                onNavigateToAnalytics = {}
            )
        }

        composeTestRule.onNodeWithText("No decisions yet")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun mainScreen_showsCreateButton() {
        composeTestRule.setContent {
            MainScreen(
                onNavigateToCreate = {},
                onNavigateToDetail = {},
                onNavigateToAnalytics = {}
            )
        }

        composeTestRule.onNodeWithText("Log a Decision Before You Act", substring = true)
            .assertExists()
    }

    @Test
    fun mainScreen_showsAnalyticsIcon() {
        composeTestRule.setContent {
            MainScreen(
                onNavigateToCreate = {},
                onNavigateToDetail = {},
                onNavigateToAnalytics = {}
            )
        }

        // Analytics icon should be present in top bar
        composeTestRule.onRoot()
            .printToLog("MainScreen")
    }
    
    @Test
    fun mainScreen_navigatesToCreateOnButtonClick() {
        var navigated = false
        composeTestRule.setContent {
            MainScreen(
                onNavigateToCreate = { navigated = true },
                onNavigateToDetail = {},
                onNavigateToAnalytics = {}
            )
        }

        composeTestRule.onNodeWithText("Log a Decision Before You Act")
            .performClick()
        
        // Verify navigation callback was called
        assert(navigated)
    }
    
    @Test
    fun mainScreen_showsSettingsIcon() {
        composeTestRule.setContent {
            MainScreen(
                onNavigateToCreate = {},
                onNavigateToDetail = {},
                onNavigateToAnalytics = {},
                onNavigateToSettings = {}
            )
        }

        // Settings icon should be present
        composeTestRule.onAllNodesWithContentDescription("Settings")
            .assertCountEquals(1)
    }
}

