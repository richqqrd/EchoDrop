package com.example.echodrop.view

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.example.echodrop.viewmodel.PaketUi
import com.example.echodrop.viewmodel.FileEntryUi
import com.example.echodrop.model.domainLayer.model.PaketId
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class PaketListItemTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun paketListItem_displaysTitleAndDescription() {
        val paket = PaketUi(
            id = PaketId("paket-123"),
            title = "Test Paket",
            description = "Beschreibung",
            tags = listOf("tag1", "tag2"),
            fileCount = 3,
            isSending = false,
            files = emptyList<FileEntryUi>(),
            ttlSeconds = 3600,
            priority = 1,
            maxHops = 5,
            currentHopCount = 0,
            createdUtc = System.currentTimeMillis()
        )

        composeRule.setContent {
            PaketListItem(
                paket = paket,
                isSending = false,
                onShare = {}
            )
        }

        composeRule.onNodeWithText("Test Paket").assertIsDisplayed()
        composeRule.onNodeWithText("Beschreibung").assertIsDisplayed()
    }
} 