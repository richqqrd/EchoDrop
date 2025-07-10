package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

/**
 * Test class for the `GetPaketDetailUseCase`.
 */
@DisplayName("GetPaketDetailUseCase Tests")
class GetPaketDetailUseCaseTest {

    private lateinit var mockPaketRepository: PaketRepository
    private lateinit var useCase: GetPaketDetailUseCase

    @BeforeEach
    fun setup() {
        mockPaketRepository = mock(PaketRepository::class.java)
        useCase = GetPaketDetailUseCase(mockPaketRepository)
    }

    @Test
    @DisplayName("invoke returns paket from repository when found")
    fun invokeReturnsPaketFromRepositoryWhenFound() = runTest {
        // Arrange
        val paketId = PaketId("test-id")
        val expectedPaket = Paket(
            id = paketId,
            meta = PaketMeta(
                title = "Test Paket",
                description = "Test description",
                tags = listOf("tag1"),
                ttlSeconds = 3600,
                priority = 5
            ),
            sizeBytes = 1000,
            fileCount = 2,
            createdUtc = 123456789L,
            files = listOf(
                FileEntry(path = "file1.txt", mime = "text/plain", sizeBytes = 500, orderIdx = 0)
            )
        )
        
        `when`(mockPaketRepository.getPaket(paketId)).thenReturn(expectedPaket)

        // Act
        val result = useCase(paketId)

        // Assert
        verify(mockPaketRepository).getPaket(paketId)
        assertEquals(expectedPaket, result)
    }

    @Test
    @DisplayName("invoke returns null when paket not found")
    fun invokeReturnsNullWhenPaketNotFound() = runTest {
        // Arrange
        val paketId = PaketId("non-existent-id")
        
        `when`(mockPaketRepository.getPaket(paketId)).thenReturn(null)

        // Act
        val result = useCase(paketId)

        // Assert
        assertNull(result)
    }

    @Test
    @DisplayName("invoke propagates exceptions from repository")
    fun invokePropagatesExceptionsFromRepository() = runTest {
        // Arrange
        val paketId = PaketId("error-id")
        val expectedException = RuntimeException("Get paket error")
        
        `when`(mockPaketRepository.getPaket(paketId)).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(paketId) }
        }
        assertEquals("Get paket error", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository exactly once")
    fun invokeCallsRepositoryExactlyOnce() = runTest {
        // Arrange
        val paketId = PaketId("test-id")
        
        `when`(mockPaketRepository.getPaket(paketId)).thenReturn(null)

        // Act
        useCase(paketId)

        // Assert
        verify(mockPaketRepository, times(1)).getPaket(paketId)
    }
} 