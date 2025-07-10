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
 * Test class for the `SavePaketUseCase`.
 */
@DisplayName("SavePaketUseCase Tests")
class SavePaketUseCaseTest {

    private lateinit var mockPaketRepository: PaketRepository
    private lateinit var useCase: SavePaketUseCase

    @BeforeEach
    fun setup() {
        mockPaketRepository = mock(PaketRepository::class.java)
        useCase = SavePaketUseCase(mockPaketRepository)
    }

    @Test
    @DisplayName("invoke calls repository upsert with correct paket")
    fun invokeCallsRepositoryUpsertWithCorrectPaket() = runTest {
        // Arrange
        val paket = Paket(
            id = PaketId("test-id"),
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

        // Act
        useCase(paket)

        // Assert
        verify(mockPaketRepository).upsert(paket)
    }

    @Test
    @DisplayName("invoke propagates exceptions from repository")
    fun invokePropagatesExceptionsFromRepository() = runTest {
        // Arrange
        val paket = Paket(
            id = PaketId("test-id"),
            meta = PaketMeta(
                title = "Test Paket",
                description = null,
                tags = emptyList(),
                ttlSeconds = 3600,
                priority = 1
            ),
            sizeBytes = 0,
            fileCount = 0,
            createdUtc = 123456789L,
            files = emptyList()
        )
        val expectedException = RuntimeException("Upsert error")
        
        doThrow(expectedException).`when`(mockPaketRepository).upsert(paket)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(paket) }
        }
        assertEquals("Upsert error", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository exactly once")
    fun invokeCallsRepositoryExactlyOnce() = runTest {
        // Arrange
        val paket = Paket(
            id = PaketId("test-id"),
            meta = PaketMeta(
                title = "Test",
                description = null,
                tags = emptyList(),
                ttlSeconds = 1800,
                priority = 3
            ),
            sizeBytes = 500,
            fileCount = 1,
            createdUtc = 123456789L,
            files = emptyList()
        )

        // Act
        useCase(paket)

        // Assert
        verify(mockPaketRepository, times(1)).upsert(paket)
    }
} 