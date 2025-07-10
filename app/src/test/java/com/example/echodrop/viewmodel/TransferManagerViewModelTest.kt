@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.usecase.network.*
import com.example.echodrop.model.domainLayer.model.ConnectionState
import com.example.echodrop.model.domainLayer.usecase.paket.ObserveInboxUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TransferManagerViewModelTest {

    private lateinit var vm: TransferManagerViewModel

    // Network use cases
    private val startBeaconing: StartBeaconingUseCase = mockk(relaxed = true)
    private val stopBeaconing: StopBeaconingUseCase = mockk(relaxed = true)
    private val observeDiscovered: ObserveDiscoveredDevicesUseCase = mockk()
    private val observeThisDevice: ObserveThisDeviceUseCase = mockk()
    private val observeConn: ObserveConnectionStateUseCase = mockk()
    private val connectToDevice: ConnectToDeviceUseCase = mockk(relaxed = true)
    private val observeForward: ObserveForwardEventsUseCase = mockk()

    // Transfer use cases
    private val observeTransfers: ObserveTransfersUseCase = mockk()
    private val pauseTransfer: PauseTransferUseCase = mockk(relaxed = true)
    private val resumeTransfer: ResumeTransferUseCase = mockk(relaxed = true)
    private val cancelTransfer: CancelTransferUseCase = mockk(relaxed = true)
    private val startTransfer: StartTransferUseCase = mockk(relaxed = true)

    // Paket use case
    private val observeInbox: ObserveInboxUseCase = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { observeDiscovered.invoke() } returns flowOf(emptyList())
        coEvery { observeThisDevice.invoke() } returns flowOf(null)
        coEvery { observeConn.invoke() } returns flowOf(ConnectionState(isConnected = false, connectedDevices = emptySet()))
        coEvery { observeTransfers.invoke() } returns flowOf(emptyList())
        coEvery { observeInbox.invoke() } returns flowOf(emptyList())
        coEvery { observeForward.invoke() } returns emptyFlow()

        vm = TransferManagerViewModel(
            startBeaconing,
            stopBeaconing,
            observeDiscovered,
            observeThisDevice,
            observeConn,
            connectToDevice,
            observeTransfers,
            pauseTransfer,
            resumeTransfer,
            cancelTransfer,
            observeInbox,
            startTransfer,
            observeForward
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }
} 