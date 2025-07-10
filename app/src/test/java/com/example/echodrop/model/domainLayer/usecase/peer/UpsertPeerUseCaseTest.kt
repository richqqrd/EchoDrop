package com.example.echodrop.model.domainLayer.usecase.peer

import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

@DisplayName("UpsertPeerUseCase Tests")
class UpsertPeerUseCaseTest {

    private lateinit var mockPeerRepository: PeerRepository
    private lateinit var useCase: UpsertPeerUseCase

    @BeforeEach
    fun setup() {
        mockPeerRepository = mock(PeerRepository::class.java)
        useCase = UpsertPeerUseCase(mockPeerRepository)
    }

    @Test
    @DisplayName("invoke calls repository upsertPeer with correct peer")
    fun invokeCallsRepositoryUpsertPeerWithCorrectPeer() = runTest {
        // Arrange
        val testPeer = Peer(
            id = PeerId("upsert-peer-123"),
            alias = "Upsert Peer",
            lastSeenUtc = System.currentTimeMillis()
        )

        // Act
        useCase(testPeer)

        // Assert
        verify(mockPeerRepository).upsertPeer(testPeer)
    }

    @Test
    @DisplayName("invoke handles peer with null alias")
    fun invokeHandlesPeerWithNullAlias() = runTest {
        // Arrange
        val testPeer = Peer(
            id = PeerId("null-alias-upsert"),
            alias = null,
            lastSeenUtc = System.currentTimeMillis()
        )

        // Act
        useCase(testPeer)

        // Assert
        verify(mockPeerRepository).upsertPeer(testPeer)
    }

    @Test
    @DisplayName("invoke propagates repository exceptions")
    fun invokePropagatesRepositoryExceptions() = runTest {
        // Arrange
        val testPeer = Peer(
            id = PeerId("error-upsert-456"),
            alias = "Error Upsert",
            lastSeenUtc = System.currentTimeMillis()
        )
        val expectedException = RuntimeException("Upsert peer failed")
        doThrow(expectedException).`when`(mockPeerRepository).upsertPeer(testPeer)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(testPeer) }
        }
        assertEquals("Upsert peer failed", exception.message)
    }

    @Test
    @DisplayName("invoke calls repository upsertPeer exactly once")
    fun invokeCallsRepositoryUpsertPeerExactlyOnce() = runTest {
        // Arrange
        val testPeer = Peer(
            id = PeerId("count-upsert-789"),
            alias = "Count Upsert",
            lastSeenUtc = System.currentTimeMillis()
        )

        // Act
        useCase(testPeer)

        // Assert
        verify(mockPeerRepository, times(1)).upsertPeer(testPeer)
    }
} 