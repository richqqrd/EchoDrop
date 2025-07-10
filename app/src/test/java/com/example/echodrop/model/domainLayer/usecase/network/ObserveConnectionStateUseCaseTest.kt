package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.model.ConnectionState
import com.example.echodrop.model.domainLayer.transport.TransportManager
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
 * Test class for the `ObserveConnectionStateUseCase`.
 */
@DisplayName("ObserveConnectionStateUseCase Tests")
class ObserveConnectionStateUseCaseTest {

    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: ObserveConnectionStateUseCase

    @BeforeEach
    fun setup() {
        mockTransportManager = mock(TransportManager::class.java)
        useCase = ObserveConnectionStateUseCase(mockTransportManager)
    }

    @Test
    @DisplayName("invoke returns flow from transportManager")
    fun invokeReturnsFlowFromTransportManager() = runTest {
        // Arrange
        val connectionState = ConnectionState(
            isConnected = true,
            connectedDevices = setOf("AA:BB:CC:DD:EE:FF"),
            groupOwnerAddress = "192.168.49.1",
            isGroupOwner = true
        )
        val expectedFlow: Flow<ConnectionState> = flowOf(connectionState)
        
        `when`(mockTransportManager.observeConnectionState()).thenReturn(expectedFlow)

        // Act
        val result = useCase()

        // Assert
        verify(mockTransportManager).observeConnectionState()
        assertEquals(expectedFlow, result)
    }

    @Test
    @DisplayName("invoke propagates exceptions from transportManager")
    fun invokePropagatesExceptionsFromTransportManager() = runTest {
        // Arrange
        val expectedException = RuntimeException("Connection state error")
        
        `when`(mockTransportManager.observeConnectionState()).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            useCase()
        }
        assertEquals("Connection state error", exception.message)
    }

    @Test
    @DisplayName("invoke calls transportManager exactly once")
    fun invokeCallsTransportManagerExactlyOnce() = runTest {
        // Arrange
        val connectionState = ConnectionState()
        val expectedFlow: Flow<ConnectionState> = flowOf(connectionState)
        
        `when`(mockTransportManager.observeConnectionState()).thenReturn(expectedFlow)

        // Act
        useCase()

        // Assert
        verify(mockTransportManager, times(1)).observeConnectionState()
    }
} 