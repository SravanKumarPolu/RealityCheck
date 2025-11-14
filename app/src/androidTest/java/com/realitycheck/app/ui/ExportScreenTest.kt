package com.realitycheck.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.hilt.android.testing.HiltAndroidRule
import androidx.hilt.android.testing.HiltAndroidTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.realitycheck.app.ui.screens.ExportScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class ExportScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun exportScreen_showsTitle() {
        composeTestRule.setContent {
            ExportScreen(
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Export Data", substring = true)
            .assertExists()
    }

    @Test
    fun exportScreen_showsCsvOption() {
        composeTestRule.setContent {
            ExportScreen(
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Export as CSV", substring = true)
            .assertExists()
    }

    @Test
    fun exportScreen_showsJsonOption() {
        composeTestRule.setContent {
            ExportScreen(
                onNavigateBack = {}
            )
        }

        composeTestRule.onNodeWithText("Export as JSON", substring = true)
            .assertExists()
    }
}

