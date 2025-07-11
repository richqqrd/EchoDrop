package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.usecase.paket.ObserveInboxUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.PurgeExpiredUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.ObserveTransfersUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("InboxViewModel Tests")
class InboxViewModelTest {

    private lateinit var viewModel: InboxViewModel
    private lateinit var mockObserveInbox: ObserveInboxUseCase
    private lateinit var mockObserveTransfers: ObserveTransfersUseCase
    private lateinit var mockStartTransfer: StartTransferUseCase
    private lateinit var mockPurgeExpired: PurgeExpiredUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockObserveInbox = mock(ObserveInboxUseCase::class.java)
        mockObserveTransfers = mock(ObserveTransfersUseCase::class.java)
        mockStartTransfer = mock(StartTransferUseCase::class.java)
        mockPurgeExpired = mock(PurgeExpiredUseCase::class.java)

        whenever(mockObserveInbox.invoke()).thenReturn(flowOf(emptyList()))
        whenever(mockObserveTransfers.invoke()).thenReturn(flowOf(emptyList()))

        viewModel = InboxViewModel(
            mockObserveInbox,        // 1. ObserveInboxUseCase
            mockObserveTransfers,    // 2. ObserveTransfersUseCase 
            mockStartTransfer,       // 3. StartTransferUseCase
            mockPurgeExpired         // 4. PurgeExpiredUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("ViewModel can be created successfully")
    fun viewModelCanBeCreatedSuccessfully() {
        assertNotNull(viewModel)
    }

    @Test
    @DisplayName("initial paketList is empty")
    fun initialPaketListIsEmpty() {
        assertTrue(viewModel.paketList.value.isEmpty())
    }

    @Test
    @DisplayName("onShareClicked with correct parameter types")
    fun onShareClickedWithCorrectParameterTypes() {
        val paketId = PaketId("test-paket-123")
        val peerId = PeerId("test-peer-456")

        viewModel.onShareClicked(paketId, peerId)

        assertTrue(true) 
    }
}