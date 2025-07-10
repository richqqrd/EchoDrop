package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferDirection
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import com.example.echodrop.model.domainLayer.transport.TransportManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

/**
 * Test class for the `StartTransferUseCase`.
 */
@DisplayName("StartTransferUseCase Tests")
class StartTransferUseCaseTest {

    private lateinit var mockTransferRepository: TransferRepository
    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: StartTransferUseCase

    @BeforeEach
    fun setup() {
        mockTransferRepository = mock(TransferRepository::class.java)
        mockTransportManager = mock(TransportManager::class.java)
        useCase = StartTransferUseCase(mockTransferRepository, mockTransportManager)
    }

    @Test
    @DisplayName("invoke calls repository startTransfer and transportManager sendPaket")
    fun invokeCallsRepositoryStartTransferAndTransportManagerSendPaket() = runTest {
        // Arrange
        val paketId = PaketId("test-paket")
        val peerId = PeerId("test-peer")

        // Act
        useCase(paketId, peerId)

        // Assert
        verify(mockTransferRepository).startTransfer(paketId, peerId, TransferDirection.OUTGOING)
        verify(mockTransportManager).sendPaket(paketId, peerId)
    }

    @Test
    @DisplayName("invoke updates state to FAILED when transportManager throws exception")
    fun invokeUpdatesStateToFailedWhenTransportManagerThrowsException() = runTest {
        // Arrange
        val paketId = PaketId("test-paket")
        val peerId = PeerId("test-peer")
        val expectedException = RuntimeException("Send failed")
        
        doThrow(expectedException).`when`(mockTransportManager).sendPaket(paketId, peerId)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(paketId, peerId) }
        }
        
        verify(mockTransferRepository).startTransfer(paketId, peerId, TransferDirection.OUTGOING)
        verify(mockTransferRepository).updateState(paketId, peerId, TransferState.FAILED)
        assertEquals("Send failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls methods in correct order")
    fun invokeCallsMethodsInCorrectOrder() = runTest {
        // Arrange
        val paketId = PaketId("test-paket")
        val peerId = PeerId("test-peer")

        // Act
        useCase(paketId, peerId)

        // Assert
        val inOrder = inOrder(mockTransferRepository, mockTransportManager)
        inOrder.verify(mockTransferRepository).startTransfer(paketId, peerId, TransferDirection.OUTGOING)
        inOrder.verify(mockTransportManager).sendPaket(paketId, peerId)
    }
} 