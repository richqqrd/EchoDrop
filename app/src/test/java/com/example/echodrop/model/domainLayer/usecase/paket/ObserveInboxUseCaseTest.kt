package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

/**
 * Test class for the `ObserveInboxUseCase`.
 */
@DisplayName("ObserveInboxUseCase Tests")
class ObserveInboxUseCaseTest {

    private lateinit var mockPaketRepository: PaketRepository
    private lateinit var useCase: ObserveInboxUseCase

    @BeforeEach
    fun setup() {
        mockPaketRepository = mock(PaketRepository::class.java)
        useCase = ObserveInboxUseCase(mockPaketRepository)
    }

    @Test
    @DisplayName("invoke returns flow from repository")
    fun invokeReturnsFlowFromRepository() = runTest {
        // Arrange
        val pakete = listOf(
            Paket(
                id = PaketId("paket1"),
                meta = PaketMeta(
                    title = "Paket 1",
                    description = "Description 1",
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
        )
        val expectedFlow: Flow<List<Paket>> = flowOf(pakete)
        
        `when`(mockPaketRepository.observeInbox()).thenReturn(expectedFlow)

        // Act
        val result = useCase()

        // Assert
        verify(mockPaketRepository).observeInbox()
        assertEquals(expectedFlow, result)
    }

    @Test
    @DisplayName("invoke propagates exceptions from repository")
    fun invokePropagatesExceptionsFromRepository() = runTest {
        // Arrange
        val expectedException = RuntimeException("Observe inbox error")
        
        `when`(mockPaketRepository.observeInbox()).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            useCase()
        }
        assertEquals("Observe inbox error", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository exactly once")
    fun invokeCallsRepositoryExactlyOnce() = runTest {
        // Arrange
        val expectedFlow: Flow<List<Paket>> = flowOf(emptyList())
        
        `when`(mockPaketRepository.observeInbox()).thenReturn(expectedFlow)

        // Act
        useCase()

        // Assert
        verify(mockPaketRepository, times(1)).observeInbox()
    }
} 