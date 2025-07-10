package com.example.echodrop.model.domainLayer.usecase.file

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.repository.FileRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

/**
 * Test class for the `GetFilesForPaketUseCase`.
 */
@DisplayName("GetFilesForPaketUseCase Tests")
class GetFilesForPaketUseCaseTest {

    private lateinit var mockFileRepository: FileRepository
    private lateinit var useCase: GetFilesForPaketUseCase

    private val testPaketId = PaketId("test-paket-123")

    @BeforeEach
    fun setup() {
        mockFileRepository = mock(FileRepository::class.java)
        useCase = GetFilesForPaketUseCase(mockFileRepository)
    }

    @Test
    @DisplayName("invoke returns files from repository")
    fun invokeReturnsFilesFromRepository() = runTest {
        // Arrange
        val expectedFiles = listOf(
            FileEntry("/data/file1.txt", "text/plain", 1024L, 0),
            FileEntry("/data/file2.jpg", "image/jpeg", 2048L, 1)
        )
        whenever(mockFileRepository.getFilesFor(testPaketId)).thenReturn(expectedFiles)

        // Act
        val result = useCase(testPaketId)

        // Assert
        assertEquals(expectedFiles, result)
        verify(mockFileRepository).getFilesFor(testPaketId)
    }

    @Test
    @DisplayName("invoke returns empty list when no files found")
    fun invokeReturnsEmptyListWhenNoFilesFound() = runTest {
        // Arrange
        whenever(mockFileRepository.getFilesFor(testPaketId)).thenReturn(emptyList())

        // Act
        val result = useCase(testPaketId)

        // Assert
        assertTrue(result.isEmpty())
        verify(mockFileRepository).getFilesFor(testPaketId)
    }

    @Test
    @DisplayName("invoke calls repository with correct paket ID")
    fun invokeCallsRepositoryWithCorrectPaketId() = runTest {
        // Arrange
        val differentPaketId = PaketId("different-paket-456")
        whenever(mockFileRepository.getFilesFor(differentPaketId)).thenReturn(emptyList())

        // Act
        useCase(differentPaketId)

        // Assert
        verify(mockFileRepository).getFilesFor(differentPaketId)
        verify(mockFileRepository, never()).getFilesFor(testPaketId)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val expectedException = RuntimeException("Database connection failed")
        whenever(mockFileRepository.getFilesFor(testPaketId)).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testPaketId) }
        }
        assertEquals("Database connection failed", exception.message)
        verify(mockFileRepository).getFilesFor(testPaketId)
    }

    @Test
    @DisplayName("invoke handles multiple calls with different paket IDs")
    fun invokeHandlesMultipleCallsWithDifferentPaketIds() = runTest {
        // Arrange
        val paketId1 = PaketId("paket-1")
        val paketId2 = PaketId("paket-2")
        val files1 = listOf(FileEntry("/data/file1.txt", "text/plain", 100L, 0))
        val files2 = listOf(FileEntry("/data/file2.jpg", "image/jpeg", 200L, 0))
        
        whenever(mockFileRepository.getFilesFor(paketId1)).thenReturn(files1)
        whenever(mockFileRepository.getFilesFor(paketId2)).thenReturn(files2)

        // Act
        val result1 = useCase(paketId1)
        val result2 = useCase(paketId2)

        // Assert
        assertEquals(files1, result1)
        assertEquals(files2, result2)
        verify(mockFileRepository).getFilesFor(paketId1)
        verify(mockFileRepository).getFilesFor(paketId2)
    }

    @Test
    @DisplayName("invoke returns files in correct order")
    fun invokeReturnsFilesInCorrectOrder() = runTest {
        // Arrange
        val orderedFiles = listOf(
            FileEntry("/data/first.txt", "text/plain", 100L, 0),
            FileEntry("/data/second.txt", "text/plain", 200L, 1),
            FileEntry("/data/third.txt", "text/plain", 300L, 2)
        )
        whenever(mockFileRepository.getFilesFor(testPaketId)).thenReturn(orderedFiles)

        // Act
        val result = useCase(testPaketId)

        // Assert
        assertEquals(3, result.size)
        assertEquals(0, result[0].orderIdx)
        assertEquals(1, result[1].orderIdx)
        assertEquals(2, result[2].orderIdx)
        assertEquals("first.txt", result[0].path.substringAfterLast("/"))
        assertEquals("second.txt", result[1].path.substringAfterLast("/"))
        assertEquals("third.txt", result[2].path.substringAfterLast("/"))
    }
} 