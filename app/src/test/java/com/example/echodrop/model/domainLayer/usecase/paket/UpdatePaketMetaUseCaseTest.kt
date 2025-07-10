package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

@DisplayName("UpdatePaketMetaUseCase Tests")
class UpdatePaketMetaUseCaseTest {

    private lateinit var mockPaketRepository: PaketRepository
    private lateinit var useCase: UpdatePaketMetaUseCase

    private val testPaketId = PaketId("test-paket-123")
    private val testTtlSeconds = 3600
    private val testPriority = 5

    @BeforeEach
    fun setup() {
        mockPaketRepository = mock(PaketRepository::class.java)
        useCase = UpdatePaketMetaUseCase(mockPaketRepository)
    }

    @Test
    @DisplayName("invoke calls repository updateMeta with correct parameters")
    fun invokeCallsRepositoryUpdateMetaWithCorrectParameters() = runTest {
        // Act
        useCase(testPaketId, testTtlSeconds, testPriority)

        // Assert
        verify(mockPaketRepository).updateMeta(testPaketId, testTtlSeconds, testPriority)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val expectedException = RuntimeException("Update failed")
        doThrow(expectedException).`when`(mockPaketRepository).updateMeta(testPaketId, testTtlSeconds, testPriority)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testPaketId, testTtlSeconds, testPriority) }
        }
        assertEquals("Update failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository updateMeta exactly once")
    fun invokeCallsRepositoryUpdateMetaExactlyOnce() = runTest {
        // Act
        useCase(testPaketId, testTtlSeconds, testPriority)

        // Assert
        verify(mockPaketRepository, times(1)).updateMeta(testPaketId, testTtlSeconds, testPriority)
    }

    @Test
    @DisplayName("invoke works with different ttl and priority values")
    fun invokeWorksWithDifferentTtlAndPriorityValues() = runTest {
        // Arrange
        val shortTtl = 1800 // 30 minutes
        val longTtl = 7200 // 2 hours
        val lowPriority = 1
        val highPriority = 10

        // Act & Assert
        useCase(testPaketId, shortTtl, lowPriority)
        verify(mockPaketRepository).updateMeta(testPaketId, shortTtl, lowPriority)

        useCase(testPaketId, longTtl, highPriority)
        verify(mockPaketRepository).updateMeta(testPaketId, longTtl, highPriority)
    }

    @Test
    @DisplayName("invoke handles edge case values correctly")
    fun invokeHandlesEdgeCaseValuesCorrectly() = runTest {
        // Arrange
        val minTtl = 0
        val maxPriority = Integer.MAX_VALUE

        // Act
        useCase(testPaketId, minTtl, maxPriority)

        // Assert
        verify(mockPaketRepository).updateMeta(testPaketId, minTtl, maxPriority)
    }
} 