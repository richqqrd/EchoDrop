package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.transport.TransportManager
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.verify

/**
 * Test class for the `ConnectToDeviceUseCase`.
 */
@DisplayName("ConnectToDeviceUseCase Tests")
class ConnectToDeviceUseCaseTest {

    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: ConnectToDeviceUseCase

    @BeforeEach
    fun setup() {
        mockTransportManager = mock(TransportManager::class.java)
        useCase = ConnectToDeviceUseCase(mockTransportManager)
    }

    @Test
    @DisplayName("invoke calls transportManager connectToDevice with correct address")
    fun invokeCallsTransportManagerConnectToDeviceWithCorrectAddress() = runTest {
        // Arrange
        val deviceAddress = "AA:BB:CC:DD:EE:FF"

        // Act
        useCase(deviceAddress)

        // Assert
        verify(mockTransportManager).connectToDevice(deviceAddress)
    }

    @Test
    @DisplayName("invoke propagates exceptions from transportManager")
    fun invokePropagatesExceptionsFromTransportManager() = runTest {
        // Arrange
        val deviceAddress = "AA:BB:CC:DD:EE:FF"
        val expectedException = RuntimeException("Connection error")
        
        doThrow(expectedException).`when`(mockTransportManager).connectToDevice(deviceAddress)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            runTest { useCase(deviceAddress) }
        }
        assertEquals("Connection error", exception.message)
    }

    @Test
    @DisplayName("invoke calls transportManager exactly once")
    fun invokeCallsTransportManagerExactlyOnce() = runTest {
        // Arrange
        val deviceAddress = "AA:BB:CC:DD:EE:FF"

        // Act
        useCase(deviceAddress)

        // Assert
        verify(mockTransportManager, times(1)).connectToDevice(deviceAddress)
    }
} 