package com.example.echodrop.model.domainLayer.usecase.peer

import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever

@DisplayName("ObservePeerUseCase Tests")
class ObservePeerUseCaseTest {

    private lateinit var mockPeerRepository: PeerRepository
    private lateinit var useCase: ObservePeerUseCase

    @BeforeEach
    fun setup() {
        mockPeerRepository = mock(PeerRepository::class.java)
        useCase = ObservePeerUseCase(mockPeerRepository)
    }

    @Test
    @DisplayName("invoke returns flow from repository observeKnownPeers")
    fun invokeReturnsFlowFromRepositoryObserveKnownPeers() = runTest {
        // Arrange
        val expectedPeers = listOf(
            Peer(
                id = PeerId("peer-1"),
                alias = "Test Peer 1",
                lastSeenUtc = System.currentTimeMillis()
            ),
            Peer(
                id = PeerId("peer-2"),
                alias = null,
                lastSeenUtc = System.currentTimeMillis() - 3600000
            )
        )
        whenever(mockPeerRepository.observeKnownPeers())
            .thenReturn(flowOf(expectedPeers))

        // Act
        val resultFlow = useCase()
        val results = resultFlow.toList()

        // Assert
        assertEquals(1, results.size)
        assertEquals(expectedPeers, results.first())
    }

    @Test
    @DisplayName("invoke handles empty peer list")
    fun invokeHandlesEmptyPeerList() = runTest {
        // Arrange
        whenever(mockPeerRepository.observeKnownPeers())
            .thenReturn(flowOf(emptyList()))

        // Act
        val resultFlow = useCase()
        val results = resultFlow.toList()

        // Assert
        assertEquals(1, results.size)
        assertTrue(results.first().isEmpty())
    }

    @Test
    @DisplayName("invoke delegates to repository exactly once")
    fun invokeDelegatesToRepositoryExactlyOnce() = runTest {
        // Arrange
        whenever(mockPeerRepository.observeKnownPeers())
            .thenReturn(flowOf(emptyList()))

        // Act
        useCase()

        // Assert
        verify(mockPeerRepository, times(1)).observeKnownPeers()
    }

    @Test
    @DisplayName("invoke handles peer list updates correctly")
    fun invokeHandlesPeerListUpdatesCorrectly() = runTest {
        // Arrange
        val initialPeers = listOf(
            Peer(
                id = PeerId("peer-1"),
                alias = "Initial Peer",
                lastSeenUtc = System.currentTimeMillis() - 7200000
            )
        )
        val updatedPeers = listOf(
            Peer(
                id = PeerId("peer-1"),
                alias = "Updated Peer",
                lastSeenUtc = System.currentTimeMillis()
            ),
            Peer(
                id = PeerId("peer-2"),
                alias = "New Peer",
                lastSeenUtc = System.currentTimeMillis()
            )
        )
        whenever(mockPeerRepository.observeKnownPeers())
            .thenReturn(flowOf(initialPeers, updatedPeers))

        // Act
        val resultFlow = useCase()
        val results = resultFlow.toList()

        // Assert
        assertEquals(2, results.size)
        assertEquals(initialPeers, results[0])
        assertEquals(updatedPeers, results[1])
    }
} 