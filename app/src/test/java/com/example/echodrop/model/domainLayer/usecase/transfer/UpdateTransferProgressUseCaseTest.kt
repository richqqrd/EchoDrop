package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

@DisplayName("UpdateTransferProgressUseCase Tests")
class UpdateTransferProgressUseCaseTest {

    private lateinit var mockTransferRepository: TransferRepository
    private lateinit var useCase: UpdateTransferProgressUseCase

    private val testPaketId = PaketId("test-paket-123")
    private val testPeerId = PeerId("test-peer-456")

    @BeforeEach
    fun setup() {
        mockTransferRepository = mock(TransferRepository::class.java)
        useCase = UpdateTransferProgressUseCase(mockTransferRepository)
    }

    @Test
    @DisplayName("invoke calls repository updateProgress with correct parameters")
    fun invokeCallsRepositoryUpdateProgressWithCorrectParameters() = runTest {
        // Arrange
        val progressPct = 50

        // Act
        useCase(testPaketId, testPeerId, progressPct)

        // Assert
        verify(mockTransferRepository).updateProgress(testPaketId, testPeerId, progressPct)
    }

    @Test
    @DisplayName("invoke handles zero progress")
    fun invokeHandlesZeroProgress() = runTest {
        // Arrange
        val progressPct = 0

        // Act
        useCase(testPaketId, testPeerId, progressPct)

        // Assert
        verify(mockTransferRepository).updateProgress(testPaketId, testPeerId, progressPct)
    }

    @Test
    @DisplayName("invoke handles complete transfer")
    fun invokeHandlesCompleteTransfer() = runTest {
        // Arrange
        val progressPct = 100

        // Act
        useCase(testPaketId, testPeerId, progressPct)

        // Assert
        verify(mockTransferRepository).updateProgress(testPaketId, testPeerId, progressPct)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val progressPct = 75
        val expectedException = RuntimeException("Update progress failed")
        doThrow(expectedException).`when`(mockTransferRepository)
            .updateProgress(testPaketId, testPeerId, progressPct)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testPaketId, testPeerId, progressPct) }
        }
        assertEquals("Update progress failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository updateProgress exactly once")
    fun invokeCallsRepositoryUpdateProgressExactlyOnce() = runTest {
        // Arrange
        val progressPct = 25

        // Act
        useCase(testPaketId, testPeerId, progressPct)

        // Assert
        verify(mockTransferRepository, times(1))
            .updateProgress(testPaketId, testPeerId, progressPct)
    }
} 