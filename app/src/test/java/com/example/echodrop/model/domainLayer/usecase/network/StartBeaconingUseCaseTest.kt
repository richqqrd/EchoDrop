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
 * Test class for the `StartBeaconingUseCase`.
 */
@DisplayName("StartBeaconingUseCase Tests")
class StartBeaconingUseCaseTest {

    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: StartBeaconingUseCase

    @BeforeEach
    fun setup() {
        mockTransportManager = mock(TransportManager::class.java)
        useCase = StartBeaconingUseCase(mockTransportManager)
    }

    @Test
    @DisplayName("invoke calls transportManager startBeaconing")
    fun invokeCallsTransportManagerStartBeaconing() = runTest {
        // Act
        useCase()

        // Assert
        verify(mockTransportManager).startBeaconing()
    }

    @Test
    @DisplayName("invoke propagates exceptions from transportManager")
    fun invokePropagatesExceptionsFromTransportManager() = runTest {
        // Arrange
        val expectedException = RuntimeException("Start beaconing error")
        
        doThrow(expectedException).`when`(mockTransportManager).startBeaconing()

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            useCase()
        }
        assertEquals("Start beaconing error", exception.message)
    }

    @Test
    @DisplayName("invoke calls transportManager exactly once")
    fun invokeCallsTransportManagerExactlyOnce() = runTest {
        // Act
        useCase()

        // Assert
        verify(mockTransportManager, times(1)).startBeaconing()
    }
} 