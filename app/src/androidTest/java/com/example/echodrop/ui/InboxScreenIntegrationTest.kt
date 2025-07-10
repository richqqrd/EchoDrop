package com.example.echodrop.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.repository.*
import com.example.echodrop.model.domainLayer.usecase.paket.*
import com.example.echodrop.model.domainLayer.usecase.transfer.ObserveTransfersUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import com.example.echodrop.view.InboxScreen
import com.example.echodrop.viewmodel.InboxViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class InboxScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    /* ------------ Mocks & Flows ------------ */
    private val paketRepo: PaketRepository = mockk(relaxed = true)
    private val transferRepo: TransferRepository = mockk(relaxed = true)

    private val inboxFlow   = MutableStateFlow<List<Paket>>(emptyList())
    private val transferFlow = MutableStateFlow<List<TransferLog>>(emptyList())

    /* ------------ Use-Cases (real) ------------ */
    private lateinit var vm: InboxViewModel

    @Before
    fun setUp() {
        // Repository-Flows stubben
        every { paketRepo.observeInbox() } returns inboxFlow
        every { transferRepo.observeTransfers() } returns transferFlow

        val observeInbox     = ObserveInboxUseCase(paketRepo)
        val observeTransfers = ObserveTransfersUseCase(transferRepo)

        // nicht Teil dieses Szenarios → als relaxed Mock
        val startTransfer: StartTransferUseCase = mockk(relaxed = true)
        val purgeExpired : PurgeExpiredUseCase  = mockk(relaxed = true)

        vm = InboxViewModel(observeInbox, observeTransfers, startTransfer, purgeExpired)
    }

    @Test
    fun uiDisplaysPaketTitle_whenUseCaseEmits() {
        // Arrange – Domain-Objekt in Flow einspeisen
        val paket = Paket(
            id = PaketId("p-1"),
            meta = PaketMeta("Demo-Titel", "Desc", emptyList(), 3600, 1),
            sizeBytes = 0,
            fileCount = 0,
            createdUtc = System.currentTimeMillis(),
            files = emptyList()
        )
        inboxFlow.value = listOf(paket)

        // Act – UI rendern
        composeRule.setContent {
            InboxScreen(
                viewModel = vm,
                onCreatePaket = {},
                onSharePaket = {},
                onOpenTransferManager = {}
            )
        }

        // Assert – Titel erscheint auf dem Screen
        composeRule.onNodeWithText("Demo-Titel").assertIsDisplayed()
    }
} 