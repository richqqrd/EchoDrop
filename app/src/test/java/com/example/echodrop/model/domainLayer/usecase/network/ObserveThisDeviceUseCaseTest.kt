package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.model.DeviceInfo
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
 * Test class for the `ObserveThisDeviceUseCase`.
 */
@DisplayName("ObserveThisDeviceUseCase Tests")
class ObserveThisDeviceUseCaseTest {

    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: ObserveThisDeviceUseCase

    @BeforeEach
    fun setup() {
        mockTransportManager = mock(TransportManager::class.java)
        useCase = ObserveThisDeviceUseCase(mockTransportManager)
    }

    @Test
    @DisplayName("invoke returns flow from transportManager")
    fun invokeReturnsFlowFromTransportManager() = runTest {
        // Arrange
        val thisDevice = DeviceInfo(
            deviceName = "MyDevice",
            deviceAddress = "AA:BB:CC:DD:EE:FF"
        )
        val expectedFlow: Flow<DeviceInfo?> = flowOf(thisDevice)
        
        `when`(mockTransportManager.observeThisDevice()).thenReturn(expectedFlow)

        // Act
        val result = useCase()

        // Assert
        verify(mockTransportManager).observeThisDevice()
        assertEquals(expectedFlow, result)
    }

    @Test
    @DisplayName("invoke returns null device flow")
    fun invokeReturnsNullDeviceFlow() = runTest {
        // Arrange
        val expectedFlow: Flow<DeviceInfo?> = flowOf(null)
        
        `when`(mockTransportManager.observeThisDevice()).thenReturn(expectedFlow)

        // Act
        val result = useCase()

        // Assert
        verify(mockTransportManager).observeThisDevice()
        assertEquals(expectedFlow, result)
    }

    @Test
    @DisplayName("invoke propagates exceptions from transportManager")
    fun invokePropagatesExceptionsFromTransportManager() = runTest {
        // Arrange
        val expectedException = RuntimeException("Device info error")
        
        `when`(mockTransportManager.observeThisDevice()).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            useCase()
        }
        assertEquals("Device info error", exception.message)
    }

    @Test
    @DisplayName("invoke calls transportManager exactly once")
    fun invokeCallsTransportManagerExactlyOnce() = runTest {
        // Arrange
        val expectedFlow: Flow<DeviceInfo?> = flowOf(null)
        
        `when`(mockTransportManager.observeThisDevice()).thenReturn(expectedFlow)

        // Act
        useCase()

        // Assert
        verify(mockTransportManager, times(1)).observeThisDevice()
    }
} 