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
import org.mockito.kotlin.verify

/**
 * Test class for the `InsertFilesUseCase`.
 */
@DisplayName("InsertFilesUseCase Tests")
class InsertFilesUseCaseTest {

    private lateinit var mockFileRepository: FileRepository
    private lateinit var useCase: InsertFilesUseCase

    private val testPaketId = PaketId("test-paket-123")

    @BeforeEach
    fun setup() {
        mockFileRepository = mock(FileRepository::class.java)
        useCase = InsertFilesUseCase(mockFileRepository)
    }

    @Test
    @DisplayName("invoke calls repository insertAll with correct parameters")
    fun invokeCallsRepositoryInsertAllWithCorrectParameters() = runTest {
        // Arrange
        val testFiles = listOf(
            FileEntry("/data/file1.txt", "text/plain", 1024L, 0),
            FileEntry("/data/file2.jpg", "image/jpeg", 2048L, 1)
        )

        // Act
        useCase(testPaketId, testFiles)

        // Assert
        verify(mockFileRepository).insertAll(testPaketId, testFiles)
    }

    @Test
    @DisplayName("invoke handles empty file list")
    fun invokeHandlesEmptyFileList() = runTest {
        // Arrange
        val emptyFiles = emptyList<FileEntry>()

        // Act
        useCase(testPaketId, emptyFiles)

        // Assert
        verify(mockFileRepository).insertAll(testPaketId, emptyFiles)
    }

} 