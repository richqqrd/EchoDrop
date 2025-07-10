package com.example.echodrop.view

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class FullscreenLoadingTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fullscreenLoading_showsMessage() {
        val message = "Bitte warten…"
        composeRule.setContent {
            FullscreenLoading(message)
        }

        composeRule.onNodeWithText(message).assertIsDisplayed()
    }
} 