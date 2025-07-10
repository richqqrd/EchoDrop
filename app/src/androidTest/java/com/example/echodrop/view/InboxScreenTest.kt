package com.example.echodrop.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.viewmodel.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4


@RunWith(AndroidJUnit4::class)
class InboxScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var fakeVm: InboxViewModel

    @Before
    fun setUp() {
        fakeVm = mockk(relaxed = true)

        // Paket-Beispieldaten
        val paket = PaketUi(
            id = PaketId("paket-123"),
            title = "Test Paket",
            description = "Beschreibung",
            tags = emptyList(),
            fileCount = 2,
            isSending = false,
            files = emptyList(),
            ttlSeconds = 3600,
            priority = 1,
            maxHops = 3,
            currentHopCount = 0,
            createdUtc = System.currentTimeMillis()
        )

        every { fakeVm.paketList } returns MutableStateFlow(listOf(paket))
        every { fakeVm.transferLogs } returns MutableStateFlow(emptyList<TransferLogUi>())
    }

    @Test
    fun inboxScreen_displaysBasicUiElements() {
        composeRule.setContent {
            InboxScreen(
                viewModel = fakeVm,
                onCreatePaket = {},
                onSharePaket = {},
                onOpenTransferManager = {}
            )
        }

        // Top-Bar-Titel
        composeRule.onNodeWithText("EchoDrop").assertIsDisplayed()

        // Floating-Action-Button (Icon Add mit CD „Create Paket“)
        composeRule.onNodeWithContentDescription("Create Paket").assertIsDisplayed()

        // Transfer-Manager-Icon-Button
        composeRule.onNodeWithContentDescription("Transfer Manager").assertIsDisplayed()

        // Paket-Titel aus der Liste
        composeRule.onNodeWithText("Test Paket").assertIsDisplayed()
    }
}