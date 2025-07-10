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
 * Test class for the `ObserveDiscoveredDevicesUseCase`.
 */
@DisplayName("ObserveDiscoveredDevicesUseCase Tests")
class ObserveDiscoveredDevicesUseCaseTest {

    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: ObserveDiscoveredDevicesUseCase

    @BeforeEach
    fun setup() {
        mockTransportManager = mock(TransportManager::class.java)
        useCase = ObserveDiscoveredDevicesUseCase(mockTransportManager)
    }

    @Test
    @DisplayName("invoke returns flow from transportManager")
    fun invokeReturnsFlowFromTransportManager() = runTest {
        // Arrange
        val devices = listOf(
            DeviceInfo(deviceName = "Device1", deviceAddress = "AA:BB:CC:DD:EE:FF"),
            DeviceInfo(deviceName = "Device2", deviceAddress = "11:22:33:44:55:66")
        )
        val expectedFlow: Flow<List<DeviceInfo>> = flowOf(devices)
        
        `when`(mockTransportManager.observeDiscoveredDevices()).thenReturn(expectedFlow)

        // Act
        val result = useCase()

        // Assert
        verify(mockTransportManager).observeDiscoveredDevices()
        assertEquals(expectedFlow, result)
    }

    @Test
    @DisplayName("invoke propagates exceptions from transportManager")
    fun invokePropagatesExceptionsFromTransportManager() = runTest {
        // Arrange
        val expectedException = RuntimeException("Discovery error")
        
        `when`(mockTransportManager.observeDiscoveredDevices()).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            useCase()
        }
        assertEquals("Discovery error", exception.message)
    }

    @Test
    @DisplayName("invoke calls transportManager exactly once")
    fun invokeCallsTransportManagerExactlyOnce() = runTest {
        // Arrange
        val expectedFlow: Flow<List<DeviceInfo>> = flowOf(emptyList())
        
        `when`(mockTransportManager.observeDiscoveredDevices()).thenReturn(expectedFlow)

        // Act
        useCase()

        // Assert
        verify(mockTransportManager, times(1)).observeDiscoveredDevices()
    }
} 