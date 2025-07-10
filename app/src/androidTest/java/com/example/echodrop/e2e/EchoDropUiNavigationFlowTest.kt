package com.example.echodrop.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI-Navigationstest (Compose):
 * 1. Startet die App (InboxScreen).
 * 2. Navigiert zum CreatePaket-Screen über FAB.
 * 3. Zurück zur Inbox via Back-Icon.
 * 4. Navigiert zum TransferManager über Toolbar-Icon.
 * 5. Zurück zur Inbox via Back-Icon.
 */
@RunWith(AndroidJUnit4::class)
class EchoDropUiNavigationFlowTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun navigateThroughMainScreens() {
        // ----- Inbox sollte initial offen sein -----
        composeRule.onNodeWithText("EchoDrop").assertIsDisplayed()

        // ----- Schritt 1: FAB ➔ CreatePaket -----
        composeRule.onNodeWithContentDescription("Create Paket").performClick()
        composeRule.onNodeWithText("Neues Paket erstellen").assertIsDisplayed()

        // Back-Pfeil anklicken
        composeRule.onNode(hasContentDescription("Zurück")).performClick()
        composeRule.onNodeWithText("EchoDrop").assertIsDisplayed()

        // ----- Schritt 2: TransferManager-Icon -----
        composeRule.onNode(hasContentDescription("Transfer Manager")).performClick()
        composeRule.onNodeWithText("Transfer-Manager").assertIsDisplayed()

        // Zurück zur Inbox
        composeRule.onNode(hasContentDescription("Zurück")).performClick()
        composeRule.onNodeWithText("EchoDrop").assertIsDisplayed()
    }
} 