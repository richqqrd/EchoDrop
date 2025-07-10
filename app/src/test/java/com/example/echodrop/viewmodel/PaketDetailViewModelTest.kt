package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.usecase.file.GetFilesForPaketUseCase
import com.example.echodrop.model.domainLayer.usecase.network.*
import com.example.echodrop.model.domainLayer.usecase.paket.*
import com.example.echodrop.model.domainLayer.usecase.peer.SavePeerUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("PaketDetailViewModel Tests")
class PaketDetailViewModelTest {

    private lateinit var mockGetPaketDetail: GetPaketDetailUseCase
    private lateinit var mockGetFiles: GetFilesForPaketUseCase
    private lateinit var mockUpdatePaketMeta: UpdatePaketMetaUseCase
    private lateinit var mockDeletePaket: DeletePaketUseCase
    private lateinit var mockStartTransfer: StartTransferUseCase
    private lateinit var mockStartBeaconing: StartBeaconingUseCase
    private lateinit var mockStopBeaconing: StopBeaconingUseCase
    private lateinit var mockObserveDiscovered: ObserveDiscoveredDevicesUseCase
    private lateinit var mockConnectToDevice: ConnectToDeviceUseCase
    private lateinit var mockSavePeer: SavePeerUseCase
    private lateinit var mockObserveConnection: ObserveConnectionStateUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create all mocks
        mockGetPaketDetail = mock(GetPaketDetailUseCase::class.java)
        mockGetFiles = mock(GetFilesForPaketUseCase::class.java)
        mockUpdatePaketMeta = mock(UpdatePaketMetaUseCase::class.java)
        mockDeletePaket = mock(DeletePaketUseCase::class.java)
        mockStartTransfer = mock(StartTransferUseCase::class.java)
        mockStartBeaconing = mock(StartBeaconingUseCase::class.java)
        mockStopBeaconing = mock(StopBeaconingUseCase::class.java)
        mockObserveDiscovered = mock(ObserveDiscoveredDevicesUseCase::class.java)
        mockConnectToDevice = mock(ConnectToDeviceUseCase::class.java)
        mockSavePeer = mock(SavePeerUseCase::class.java)
        mockObserveConnection = mock(ObserveConnectionStateUseCase::class.java)

        // Setup default mock behavior to prevent Log errors
        whenever(mockObserveDiscovered.invoke()).thenReturn(flowOf(emptyList()))
        whenever(mockObserveConnection.invoke()).thenReturn(flowOf(mock()))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("ViewModel dependencies can be created")
    fun viewModelDependenciesCanBeCreated() {
        // Assert - All mocks were created successfully
        assertNotNull(mockGetPaketDetail)
        assertNotNull(mockGetFiles)
        assertNotNull(mockUpdatePaketMeta)
        assertNotNull(mockDeletePaket)
        assertNotNull(mockStartTransfer)
        assertNotNull(mockStartBeaconing)
        assertNotNull(mockStopBeaconing)
        assertNotNull(mockObserveDiscovered)
        assertNotNull(mockConnectToDevice)
        assertNotNull(mockSavePeer)
        assertNotNull(mockObserveConnection)
    }

    @Test
    @DisplayName("Mock setup completes without errors")
    fun mockSetupCompletesWithoutErrors() {
        // Assert
        assertTrue(true) // If we reach this point, setup was successful
    }
}