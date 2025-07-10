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

@DisplayName("ResumeTransferUseCase Tests")
class ResumeTransferUseCaseTest {

    private lateinit var mockTransferRepository: TransferRepository
    private lateinit var useCase: ResumeTransferUseCase

    private val testPaketId = PaketId("test-paket-123")
    private val testPeerId = PeerId("test-peer-456")

    @BeforeEach
    fun setup() {
        mockTransferRepository = mock(TransferRepository::class.java)
        useCase = ResumeTransferUseCase(mockTransferRepository)
    }

    @Test
    @DisplayName("invoke calls repository resume with correct parameters")
    fun invokeCallsRepositoryResumeWithCorrectParameters() = runTest {
        // Act
        useCase(testPaketId, testPeerId)

        // Assert
        verify(mockTransferRepository).resume(testPaketId, testPeerId)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val expectedException = RuntimeException("Resume transfer failed")
        doThrow(expectedException).`when`(mockTransferRepository).resume(testPaketId, testPeerId)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testPaketId, testPeerId) }
        }
        assertEquals("Resume transfer failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository resume exactly once")
    fun invokeCallsRepositoryResumeExactlyOnce() = runTest {
        // Act
        useCase(testPaketId, testPeerId)

        // Assert
        verify(mockTransferRepository, times(1)).resume(testPaketId, testPeerId)
    }
} 