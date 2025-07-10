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
 * Test class for the `StopBeaconingUseCase`.
 */
@DisplayName("StopBeaconingUseCase Tests")
class StopBeaconingUseCaseTest {

    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: StopBeaconingUseCase

    @BeforeEach
    fun setup() {
        mockTransportManager = mock(TransportManager::class.java)
        useCase = StopBeaconingUseCase(mockTransportManager)
    }

    @Test
    @DisplayName("invoke calls transportManager stopBeaconing")
    fun invokeCallsTransportManagerStopBeaconing() = runTest {
        // Act
        useCase()

        // Assert
        verify(mockTransportManager).stopBeaconing()
    }

    @Test
    @DisplayName("invoke propagates exceptions from transportManager")
    fun invokePropagatesExceptionsFromTransportManager() = runTest {
        // Arrange
        val expectedException = RuntimeException("Stop beaconing error")
        
        doThrow(expectedException).`when`(mockTransportManager).stopBeaconing()

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            useCase()
        }
        assertEquals("Stop beaconing error", exception.message)
    }

    @Test
    @DisplayName("invoke calls transportManager exactly once")
    fun invokeCallsTransportManagerExactlyOnce() = runTest {
        // Act
        useCase()

        // Assert
        verify(mockTransportManager, times(1)).stopBeaconing()
    }
} 