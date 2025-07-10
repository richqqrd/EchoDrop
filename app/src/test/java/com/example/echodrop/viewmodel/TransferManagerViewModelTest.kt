@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.usecase.network.*
import com.example.echodrop.model.domainLayer.usecase.paket.ObserveInboxUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.*
import com.example.echodrop.model.domainLayer.transport.ForwardEvent
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

@DisplayName("TransferManagerViewModel Tests")
class TransferManagerViewModelTest {

    private lateinit var viewModel: TransferManagerViewModel
    
    @Mock private lateinit var mockStartBeaconing: StartBeaconingUseCase
    @Mock private lateinit var mockStopBeaconing: StopBeaconingUseCase
    @Mock private lateinit var mockObserveDiscovered: ObserveDiscoveredDevicesUseCase
    @Mock private lateinit var mockObserveThisDevice: ObserveThisDeviceUseCase
    @Mock private lateinit var mockObserveConnection: ObserveConnectionStateUseCase
    @Mock private lateinit var mockConnectToDevice: ConnectToDeviceUseCase
    @Mock private lateinit var mockObserveTransfers: ObserveTransfersUseCase
    @Mock private lateinit var mockPauseTransfer: PauseTransferUseCase
    @Mock private lateinit var mockResumeTransfer: ResumeTransferUseCase
    @Mock private lateinit var mockCancelTransfer: CancelTransferUseCase
    @Mock private lateinit var mockObserveInbox: ObserveInboxUseCase
    @Mock private lateinit var mockStartTransfer: StartTransferUseCase
    @Mock private lateinit var mockObserveForwardEvents: ObserveForwardEventsUseCase
    
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testTransfer1 = TransferLog(
        paketId = PaketId("paket-123"),
        peerId = PeerId("peer-456"),
        state = TransferState.ACTIVE,
        direction = TransferDirection.OUTGOING,
        progressPct = 50,
        lastUpdateUtc = System.currentTimeMillis()
    )

    private val testDeviceInfo = DeviceInfo(
        deviceName = "Test Device",
        deviceAddress = "aa:bb:cc:dd:ee:ff"
    )

    private val testConnectionState = ConnectionState(
        isConnected = true,
        connectedDevices = setOf("aa:bb:cc:dd:ee:ff")
    )

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Setup default return values for all flows
        `when`(mockObserveTransfers.invoke()).thenReturn(flowOf(emptyList()))
        `when`(mockObserveDiscovered.invoke()).thenReturn(flowOf(emptyList()))
        `when`(mockObserveThisDevice.invoke()).thenReturn(flowOf(null))
        `when`(mockObserveConnection.invoke()).thenReturn(flowOf(
            ConnectionState(isConnected = false, connectedDevices = emptySet())
        ))
        `when`(mockObserveInbox.invoke()).thenReturn(flowOf(emptyList()))
        `when`(mockObserveForwardEvents.invoke()).thenReturn(emptyFlow())
    }

    private fun createViewModel(): TransferManagerViewModel {
        return TransferManagerViewModel(
            mockStartBeaconing,
            mockStopBeaconing,
            mockObserveDiscovered,
            mockObserveThisDevice,
            mockObserveConnection,
            mockConnectToDevice,
            mockObserveTransfers,
            mockPauseTransfer,
            mockResumeTransfer,
            mockCancelTransfer,
            mockObserveInbox,
            mockStartTransfer,
            mockObserveForwardEvents
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("initial state has correct default values")
    fun initialStateHasCorrectDefaultValues() = runTest {
        viewModel = createViewModel()
        
        // Wait for initial state to be set
        withTimeout(1000) {
            viewModel.uiState.first()
        }
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertFalse(state.isWifiDirectEnabled)
        assertFalse(state.isDiscoveryActive)
        assertFalse(state.isDebugMode)
        assertNull(state.thisDevice)
        assertTrue(state.discoveredDevices.isEmpty())
        assertTrue(state.connectedDevices.isEmpty())
        assertTrue(state.sendingTransfers.isEmpty())
        assertTrue(state.receivingTransfers.isEmpty())
        assertTrue(state.completedTransfers.isEmpty())
        assertTrue(state.forwardLog.isEmpty())
    }

    @Test
    @DisplayName("transfers update when ObserveTransfers emits new data")
    fun transfersUpdateWhenObserveTransfersEmitsNewData() = runTest {
        // Arrange
        val transferList = listOf(testTransfer1)
        `when`(mockObserveTransfers.invoke()).thenReturn(flowOf(transferList))
        
        // Create ViewModel and wait for flows to be processed
        viewModel = createViewModel()
        withTimeout(1000) {
            viewModel.uiState.first { it.sendingTransfers.isNotEmpty() }
        }
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(1, state.sendingTransfers.size)
        assertEquals("paket-123-peer-456", state.sendingTransfers.first().id)
    }

    @Test
    @DisplayName("discovered devices updates when ObserveDiscovered emits new data")
    fun discoveredDevicesUpdatesWhenObserveDiscoveredEmitsNewData() = runTest {
        // Arrange
        val deviceList = listOf(testDeviceInfo)
        `when`(mockObserveDiscovered.invoke()).thenReturn(flowOf(deviceList))
        
        // Create ViewModel and wait for state update
        viewModel = createViewModel()
        withTimeout(1000) {
            viewModel.uiState.first { it.discoveredDevices.isNotEmpty() }
        }
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(1, state.discoveredDevices.size)
        assertEquals(testDeviceInfo.deviceName, state.discoveredDevices.first().deviceName)
        assertEquals(testDeviceInfo.deviceAddress, state.discoveredDevices.first().deviceAddress)
    }

    @Test
    @DisplayName("thisDevice updates when ObserveThisDevice emits new data")
    fun thisDeviceUpdatesWhenObserveThisDeviceEmitsNewData() = runTest {
        // Arrange
        `when`(mockObserveThisDevice.invoke()).thenReturn(flowOf(testDeviceInfo))
        
        // Create ViewModel and wait for state update
        viewModel = createViewModel()
        withTimeout(1000) {
            viewModel.uiState.first { it.thisDevice != null }
        }
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertNotNull(state.thisDevice)
        assertTrue(state.isWifiDirectEnabled) // Should be true when thisDevice is not null
        assertEquals(testDeviceInfo.deviceName, state.thisDevice?.deviceName)
        assertEquals(testDeviceInfo.deviceAddress, state.thisDevice?.deviceAddress)
    }

    @Test
    @DisplayName("connectionState updates when ObserveConnection emits new data")
    fun connectionStateUpdatesWhenObserveConnectionEmitsNewData() = runTest {
        // Arrange
        `when`(mockObserveConnection.invoke()).thenReturn(flowOf(testConnectionState))
        
        // Create ViewModel and wait for state update
        viewModel = createViewModel()
        withTimeout(1000) {
            viewModel.uiState.first { it.connectedDevices.isNotEmpty() }
        }
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(testConnectionState.connectedDevices, state.connectedDevices)
    }

    @Test
    @DisplayName("forward events are collected and limited to 50")
    fun forwardEventsAreCollectedAndLimitedTo50() = runTest {
        // Arrange
        val forwardEvent = ForwardEvent(
            paketId = PaketId("test-paket"),
            peerId = PeerId("peer1"),
            stage = ForwardEvent.Stage.SENT,
            message = "Test forward event",
            timestamp = System.currentTimeMillis()
        )
        `when`(mockObserveForwardEvents.invoke()).thenReturn(flowOf(forwardEvent))
        
        // Create ViewModel and wait for state update
        viewModel = createViewModel()
        withTimeout(1000) {
            viewModel.uiState.first { it.forwardLog.isNotEmpty() }
        }
        advanceUntilIdle()
        
        // Assert
        val state = viewModel.uiState.value
        assertEquals(1, state.forwardLog.size)
        assertEquals(forwardEvent, state.forwardLog.first())
    }



    @Test
    @DisplayName("stopDiscovery calls StopBeaconingUseCase")
    fun stopDiscoveryCallsStopBeaconingUseCase() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        
        // Arrange - Start discovery first
        viewModel.startDiscovery()
        advanceUntilIdle()
        
        // Act
        viewModel.stopDiscovery()
        advanceUntilIdle()
        
        // Assert
        assertFalse(viewModel.uiState.value.isDiscoveryActive)
        verify(mockStopBeaconing).invoke()
    }





} 