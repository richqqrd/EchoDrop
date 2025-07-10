package com.example.echodrop.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.repository.FileRepository
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import com.example.echodrop.model.domainLayer.usecase.file.GetFilesForPaketUseCase
import com.example.echodrop.model.domainLayer.usecase.network.*
import com.example.echodrop.model.domainLayer.usecase.paket.*
import com.example.echodrop.model.domainLayer.usecase.peer.SavePeerUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import com.example.echodrop.view.PaketDetailScreen
import com.example.echodrop.viewmodel.PaketDetailViewModel
import io.mockk.every          // für normale (nicht-suspend) Aufrufe
import io.mockk.coEvery        // für suspend-Funktionen
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaketDetailScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var vm: PaketDetailViewModel

    @Before
    fun setUp() {
        /* ---------- Repositories ---------- */
        val paketRepo: PaketRepository = mockk(relaxed = true)
        val fileRepo : FileRepository  = mockk(relaxed = true)

        val paketId = PaketId("p-1")
        val paket = Paket(
            id          = paketId,
            meta        = PaketMeta("Demo-Paket", "Desc", emptyList(), 3600, 1),
            sizeBytes   = 0,
            fileCount   = 0,
            createdUtc  = System.currentTimeMillis(),
            files       = emptyList()
        )
        coEvery { paketRepo.getPaket(paketId) } returns paket
        coEvery { fileRepo.getFilesFor(paketId) } returns emptyList()

        /* ---------- Haupt-Use-Cases ---------- */
        val getPaketDetail   = GetPaketDetailUseCase(paketRepo)
        val getFilesForPaket = GetFilesForPaketUseCase(fileRepo)

        /* ---------- übrige abhängige Use-Cases (relaxed mocks) ---------- */
        val updateMeta   : UpdatePaketMetaUseCase      = mockk(relaxed = true)
        val deletePaket  : DeletePaketUseCase          = mockk(relaxed = true)
        val startTransfer: StartTransferUseCase        = mockk(relaxed = true)
        val startBeacon  : StartBeaconingUseCase       = mockk(relaxed = true)
        val stopBeacon   : StopBeaconingUseCase        = mockk(relaxed = true)
        val observeDisc  : ObserveDiscoveredDevicesUseCase = mockk()
        val connectToDev : ConnectToDeviceUseCase      = mockk(relaxed = true)
        val savePeer     : SavePeerUseCase             = mockk(relaxed = true)
        val observeConn  : ObserveConnectionStateUseCase = mockk()

        every { observeDisc.invoke() } returns flowOf(emptyList())
        every { observeConn.invoke() } returns flowOf(ConnectionState())

        /* ---------- ViewModel ---------- */
        vm = PaketDetailViewModel(
            getPaketDetail,
            getFilesForPaket,
            updateMeta,
            deletePaket,
            startTransfer,
            startBeacon,
            stopBeacon,
            observeDisc,
            connectToDev,
            savePeer,
            observeConn
        )
    }

    @Test
    fun paketTitleIsShownAfterLoad() {
        composeRule.setContent {
            PaketDetailScreen(
                paketId = "p-1",
                onBackClick = {},
                onOpenTransferManager = {},
                viewModel = vm
            )
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule.onNodeWithText("Demo-Paket").assertExists()
                true               // Node gefunden
            } catch (e: AssertionError) {
                false              // Noch nicht da → weiter warten
            }
        }
        composeRule.onNodeWithText("Demo-Paket").assertIsDisplayed()
    }
} 