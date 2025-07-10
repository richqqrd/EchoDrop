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

@DisplayName("PauseTransferUseCase Tests")
class PauseTransferUseCaseTest {

    private lateinit var mockTransferRepository: TransferRepository
    private lateinit var useCase: PauseTransferUseCase

    private val testPaketId = PaketId("test-paket-123")
    private val testPeerId = PeerId("test-peer-456")

    @BeforeEach
    fun setup() {
        mockTransferRepository = mock(TransferRepository::class.java)
        useCase = PauseTransferUseCase(mockTransferRepository)
    }

    @Test
    @DisplayName("invoke calls repository pause with correct parameters")
    fun invokeCallsRepositoryPauseWithCorrectParameters() = runTest {
        // Act
        useCase(testPaketId, testPeerId)

        // Assert
        verify(mockTransferRepository).pause(testPaketId, testPeerId)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val expectedException = RuntimeException("Pause transfer failed")
        doThrow(expectedException).`when`(mockTransferRepository).pause(testPaketId, testPeerId)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testPaketId, testPeerId) }
        }
        assertEquals("Pause transfer failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository pause exactly once")
    fun invokeCallsRepositoryPauseExactlyOnce() = runTest {
        // Act
        useCase(testPaketId, testPeerId)

        // Assert
        verify(mockTransferRepository, times(1)).pause(testPaketId, testPeerId)
    }
} 