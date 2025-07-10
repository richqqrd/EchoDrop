package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.repository.PaketRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

@DisplayName("PurgeExpiredUseCase Tests")
class PurgeExpiredUseCaseTest {

    private lateinit var mockPaketRepository: PaketRepository
    private lateinit var useCase: PurgeExpiredUseCase

    private val testNowUtc = System.currentTimeMillis()

    @BeforeEach
    fun setup() {
        mockPaketRepository = mock(PaketRepository::class.java)
        useCase = PurgeExpiredUseCase(mockPaketRepository)
    }

    @Test
    @DisplayName("invoke calls repository purgeExpire with correct timestamp")
    fun invokeCallsRepositoryPurgeExpireWithCorrectTimestamp() = runTest {
        // Arrange
        `when`(mockPaketRepository.purgeExpire(testNowUtc)).thenReturn(5)

        // Act
        val result = useCase(testNowUtc)

        // Assert
        verify(mockPaketRepository).purgeExpire(testNowUtc)
        assertEquals(5, result)
    }

    @Test
    @DisplayName("invoke returns zero when no expired pakets")
    fun invokeReturnsZeroWhenNoExpiredPakets() = runTest {
        // Arrange
        `when`(mockPaketRepository.purgeExpire(testNowUtc)).thenReturn(0)

        // Act
        val result = useCase(testNowUtc)

        // Assert
        assertEquals(0, result)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val expectedException = RuntimeException("Purge failed")
        `when`(mockPaketRepository.purgeExpire(testNowUtc)).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testNowUtc) }
        }
        assertEquals("Purge failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository purgeExpire exactly once")
    fun invokeCallsRepositoryPurgeExpireExactlyOnce() = runTest {
        // Arrange
        `when`(mockPaketRepository.purgeExpire(testNowUtc)).thenReturn(3)

        // Act
        useCase(testNowUtc)

        // Assert
        verify(mockPaketRepository, times(1)).purgeExpire(testNowUtc)
    }

    @Test
    @DisplayName("invoke works with different timestamp values")
    fun invokeWorksWithDifferentTimestampValues() = runTest {
        // Arrange
        val pastTimestamp = testNowUtc - 3600000L // 1 hour ago
        val futureTimestamp = testNowUtc + 3600000L // 1 hour from now
        
        `when`(mockPaketRepository.purgeExpire(pastTimestamp)).thenReturn(10)
        `when`(mockPaketRepository.purgeExpire(futureTimestamp)).thenReturn(0)

        // Act & Assert
        assertEquals(10, useCase(pastTimestamp))
        assertEquals(0, useCase(futureTimestamp))
        
        verify(mockPaketRepository).purgeExpire(pastTimestamp)
        verify(mockPaketRepository).purgeExpire(futureTimestamp)
    }
} 