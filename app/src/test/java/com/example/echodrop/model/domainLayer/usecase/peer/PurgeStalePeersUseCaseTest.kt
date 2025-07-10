package com.example.echodrop.model.domainLayer.usecase.peer

import com.example.echodrop.model.domainLayer.repository.PeerRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

@DisplayName("PurgeStalePeersUseCase Tests")
class PurgeStalePeersUseCaseTest {

    private lateinit var mockPeerRepository: PeerRepository
    private lateinit var useCase: PurgeStalePeersUseCase

    private val testOlderThanUtc = System.currentTimeMillis() - 86400000 // 24 hours ago

    @BeforeEach
    fun setup() {
        mockPeerRepository = mock(PeerRepository::class.java)
        useCase = PurgeStalePeersUseCase(mockPeerRepository)
    }

    @Test
    @DisplayName("invoke calls repository purgeStalePeers with correct timestamp")
    fun invokeCallsRepositoryPurgeStalePeersWithCorrectTimestamp() = runTest {
        // Act
        useCase(testOlderThanUtc)

        // Assert
        verify(mockPeerRepository).purgeStalePeers(testOlderThanUtc)
    }

    @Test
    @DisplayName("invoke handles different timestamps")
    fun invokeHandlesDifferentTimestamps() = runTest {
        // Arrange
        val customTimestamp = System.currentTimeMillis() - 3600000 // 1 hour ago

        // Act
        useCase(customTimestamp)

        // Assert
        verify(mockPeerRepository).purgeStalePeers(customTimestamp)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val expectedException = RuntimeException("Purge stale peers failed")
        doThrow(expectedException).`when`(mockPeerRepository).purgeStalePeers(testOlderThanUtc)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testOlderThanUtc) }
        }
        assertEquals("Purge stale peers failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository purgeStalePeers exactly once")
    fun invokeCallsRepositoryPurgeStalePeersExactlyOnce() = runTest {
        // Act
        useCase(testOlderThanUtc)

        // Assert
        verify(mockPeerRepository, times(1)).purgeStalePeers(testOlderThanUtc)
    }
} 