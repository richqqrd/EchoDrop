package com.example.echodrop.view

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.example.echodrop.viewmodel.PaketUi
import com.example.echodrop.viewmodel.FileEntryUi
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.viewmodel.TransferLogUi
import com.example.echodrop.viewmodel.PaketDetailViewModel
import com.example.echodrop.viewmodel.PaketDetailState
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.mockk.mockk
import com.example.echodrop.model.domainLayer.model.PeerId

@RunWith(AndroidJUnit4::class)
class PaketDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var fakeViewModel: PaketDetailViewModel

    @Before
    fun setup() {
        fakeViewModel = mockk(relaxed = true)

        val paket = PaketUi(
            id = PaketId("pak1"),
            title = "Demo Paket",
            description = "Dateien für die Demo",
            tags = listOf("demo"),
            fileCount = 1,
            isSending = false,
            files = listOf(FileEntryUi(path="/demo.pdf", mime="application/pdf", sizeBytes=100, orderIdx=0)),
            ttlSeconds = 3600,
            priority = 1,
            maxHops = 5,
            currentHopCount = 0,
            createdUtc = System.currentTimeMillis()
        )
        val stateFlow = MutableStateFlow(PaketDetailState(paket = paket, isLoading = false))
        every { fakeViewModel.state } returns stateFlow
        every { fakeViewModel.isDiscoveryActive } returns MutableStateFlow(false)
    }

    @Test
    fun paketDetailScreen_showsTitleAndDescription() {
        composeRule.setContent {
            PaketDetailScreen(
                paketId = "pak1",
                onBackClick = {},
                onOpenTransferManager = {},
                viewModel = fakeViewModel
            )
        }

        composeRule.onNodeWithText("Demo Paket").assertIsDisplayed()
        composeRule.onNodeWithText("Dateien für die Demo").assertIsDisplayed()
    }
} 