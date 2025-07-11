package com.example.echodrop.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.usecase.network.*
import com.example.echodrop.model.domainLayer.usecase.paket.ObserveInboxUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.*
import com.example.echodrop.model.domainLayer.transport.ForwardEvent
import com.example.echodrop.view.TransferManagerScreen
import com.example.echodrop.viewmodel.TransferManagerViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransferManagerScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    /* ---------- steuerbare Test-Flows ---------- */
    private val discoveredDevicesFlow = MutableStateFlow<List<DeviceInfo>>(emptyList())
    private val thisDeviceFlow        = MutableStateFlow<DeviceInfo?>(null)
    private val connectionStateFlow   = MutableStateFlow(ConnectionState())
    private val transfersFlow         = MutableStateFlow<List<TransferLog>>(emptyList())

    private lateinit var vm: TransferManagerViewModel

    @Before
    fun setUp() {
        /* ---------- Network-Use-Cases ---------- */
        val startBeacon     : StartBeaconingUseCase        = mockk(relaxed = true)
        val stopBeacon      : StopBeaconingUseCase         = mockk(relaxed = true)
        val observeDevices  : ObserveDiscoveredDevicesUseCase = mockk()
        val observeThisDev  : ObserveThisDeviceUseCase     = mockk()
        val observeConn     : ObserveConnectionStateUseCase = mockk()
        val connectToDevice : ConnectToDeviceUseCase       = mockk(relaxed = true)

        every { observeDevices.invoke() } returns discoveredDevicesFlow
        every { observeThisDev.invoke() } returns thisDeviceFlow
        every { observeConn.invoke() }   returns connectionStateFlow

        /* ---------- Transfer-Use-Cases ---------- */
        val observeTransfers: ObserveTransfersUseCase = mockk()
        every { observeTransfers.invoke() } returns transfersFlow
        val pauseTransfer  : PauseTransferUseCase  = mockk(relaxed = true)
        val resumeTransfer : ResumeTransferUseCase = mockk(relaxed = true)
        val cancelTransfer : CancelTransferUseCase = mockk(relaxed = true)

        /* ---------- Sonstige ---------- */
        val observeInbox      : ObserveInboxUseCase        = mockk(relaxed = true)
        val startTransfer     : StartTransferUseCase       = mockk(relaxed = true)
        val observeForwardEvt : ObserveForwardEventsUseCase = mockk()
        every { observeForwardEvt.invoke() } returns flowOf<ForwardEvent>()

        /* ---------- ViewModel ---------- */
        vm = TransferManagerViewModel(
            startBeacon,
            stopBeacon,
            observeDevices,
            observeThisDev,
            observeConn,
            connectToDevice,
            observeTransfers,
            pauseTransfer,
            resumeTransfer,
            cancelTransfer,
            observeInbox,
            startTransfer,
            observeForwardEvt
        )
    }

    @Test
    fun deviceCounterUpdates_whenDiscoveredDevicesFlowEmits() {
        composeRule.setContent {
            TransferManagerScreen(
                onBackClick = {},
                viewModel   = vm
            )
        }

        /* ---------- Flow-Update ---------- */
        discoveredDevicesFlow.value = listOf(DeviceInfo("Pixel 6", "AA:BB:CC:DD:EE:FF"))

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule.onNodeWithText("1").assertExists()
                true            
            } catch (e: AssertionError) {
                false       
            }
        }
        composeRule.onNodeWithText("1").assertIsDisplayed()
    }
} 