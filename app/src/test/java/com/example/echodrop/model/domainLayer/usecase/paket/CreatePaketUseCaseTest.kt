package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.FileEntry
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
 * Test class for the `CreatePaketUseCase`.
 */
@DisplayName("CreatePaketUseCase Tests")
class CreatePaketUseCaseTest {

    private lateinit var mockPaketRepository: PaketRepository
    private lateinit var useCase: CreatePaketUseCase

    @BeforeEach
    fun setup() {
        mockPaketRepository = mock(PaketRepository::class.java)
        useCase = CreatePaketUseCase(mockPaketRepository)
    }

    @Test
    @DisplayName("invoke calls repository insert with correct parameters")
    fun invokeCallsRepositoryInsertWithCorrectParameters() = runTest {
        // Arrange
        val meta = PaketMeta(
            title = "Test Paket",
            description = "Test description",
            tags = listOf("tag1", "tag2"),
            ttlSeconds = 3600,
            priority = 5
        )
        val files = listOf(
            FileEntry(path = "file1.txt", mime = "text/plain", sizeBytes = 100, orderIdx = 0),
            FileEntry(path = "file2.txt", mime = "text/plain", sizeBytes = 200, orderIdx = 1)
        )
        val expectedPaketId = PaketId("test-id")
        
        `when`(mockPaketRepository.insert(meta, files)).thenReturn(expectedPaketId)

        // Act
        val result = useCase(meta, files)

        // Assert
        verify(mockPaketRepository).insert(meta, files)
        assertEquals(expectedPaketId, result)
    }

    @Test
    @DisplayName("invoke propagates exceptions from repository")
    fun invokePropagatesExceptionsFromRepository() = runTest {
        // Arrange
        val meta = PaketMeta(
            title = "Test Paket",
            description = "Test description",
            tags = emptyList(),
            ttlSeconds = 3600,
            priority = 5
        )
        val files = emptyList<FileEntry>()
        val expectedException = RuntimeException("Insert error")
        
        `when`(mockPaketRepository.insert(meta, files)).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(meta, files) }
        }
        assertEquals("Insert error", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository exactly once")
    fun invokeCallsRepositoryExactlyOnce() = runTest {
        // Arrange
        val meta = PaketMeta(
            title = "Test Paket",
            description = null,
            tags = emptyList(),
            ttlSeconds = 3600,
            priority = 1
        )
        val files = emptyList<FileEntry>()
        val expectedPaketId = PaketId("test-id")
        
        `when`(mockPaketRepository.insert(meta, files)).thenReturn(expectedPaketId)

        // Act
        useCase(meta, files)

        // Assert
        verify(mockPaketRepository, times(1)).insert(meta, files)
    }
} 