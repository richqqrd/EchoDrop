package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferDirection
import com.example.echodrop.model.domainLayer.model.TransferLog
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

@DisplayName("ObserveTransfersUseCase Tests")
class ObserveTransfersUseCaseTest {

    private lateinit var mockTransferRepository: TransferRepository
    private lateinit var useCase: ObserveTransfersUseCase

    @BeforeEach
    fun setup() {
        mockTransferRepository = mock(TransferRepository::class.java)
        useCase = ObserveTransfersUseCase(mockTransferRepository)
    }

    @Test
    @DisplayName("invoke returns flow from repository observeTransfers")
    fun invokeReturnsFlowFromRepositoryObserveTransfers() = runTest {
        // Arrange
        val expectedTransfers = listOf(
            TransferLog(
                paketId = PaketId("paket-1"),
                peerId = PeerId("peer-1"),
                state = TransferState.QUEUED,
                direction = TransferDirection.OUTGOING,
                progressPct = 50,
                lastUpdateUtc = System.currentTimeMillis()
            ),
            TransferLog(
                paketId = PaketId("paket-2"),
                peerId = PeerId("peer-2"),
                state = TransferState.DONE,
                direction = TransferDirection.INCOMING,
                progressPct = 100,
                lastUpdateUtc = System.currentTimeMillis() - 3600000
            )
        )
        whenever(mockTransferRepository.observeTransfers())
            .thenReturn(flowOf(expectedTransfers))

        // Act
        val resultFlow = useCase()
        val results = resultFlow.toList()

        // Assert
        assertEquals(1, results.size)
        assertEquals(expectedTransfers, results.first())
    }

    @Test
    @DisplayName("invoke handles empty transfer list")
    fun invokeHandlesEmptyTransferList() = runTest {
        // Arrange
        whenever(mockTransferRepository.observeTransfers())
            .thenReturn(flowOf(emptyList()))

        // Act
        val resultFlow = useCase()
        val results = resultFlow.toList()

        // Assert
        assertEquals(1, results.size)
        assertTrue(results.first().isEmpty())
    }

    @Test
    @DisplayName("invoke delegates to repository exactly once")
    fun invokeDelegatesToRepositoryExactlyOnce() = runTest {
        // Arrange
        whenever(mockTransferRepository.observeTransfers())
            .thenReturn(flowOf(emptyList()))

        // Act
        useCase()

        // Assert
        verify(mockTransferRepository, times(1)).observeTransfers()
    }
} 