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

@DisplayName("DeletePaketUseCase Tests")
class DeletePaketUseCaseTest {

    private lateinit var mockPaketRepository: PaketRepository
    private lateinit var useCase: DeletePaketUseCase

    private val testPaketId = PaketId("test-paket-123")

    @BeforeEach
    fun setup() {
        mockPaketRepository = mock(PaketRepository::class.java)
        useCase = DeletePaketUseCase(mockPaketRepository)
    }

    @Test
    @DisplayName("invoke calls repository delete with correct paket id")
    fun invokeCallsRepositoryDeleteWithCorrectPaketId() = runTest {
        // Act
        useCase(testPaketId)

        // Assert
        verify(mockPaketRepository).delete(testPaketId)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val expectedException = RuntimeException("Delete failed")
        doThrow(expectedException).`when`(mockPaketRepository).delete(testPaketId)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testPaketId) }
        }
        assertEquals("Delete failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository delete exactly once")
    fun invokeCallsRepositoryDeleteExactlyOnce() = runTest {
        // Act
        useCase(testPaketId)

        // Assert
        verify(mockPaketRepository, times(1)).delete(testPaketId)
    }
} 