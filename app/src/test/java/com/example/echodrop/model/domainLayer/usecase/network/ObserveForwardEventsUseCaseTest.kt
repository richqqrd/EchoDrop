package com.example.echodrop.model.domainLayer.usecase.network

import com.example.echodrop.model.domainLayer.transport.ForwardEvent
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
 * Test class for the `ObserveForwardEventsUseCase`.
 */
@DisplayName("ObserveForwardEventsUseCase Tests")
class ObserveForwardEventsUseCaseTest {

    private lateinit var mockTransportManager: TransportManager
    private lateinit var useCase: ObserveForwardEventsUseCase

    @BeforeEach
    fun setup() {
        mockTransportManager = mock(TransportManager::class.java)
        useCase = ObserveForwardEventsUseCase(mockTransportManager)
    }

    @Test
    @DisplayName("invoke returns flow from transportManager")
    fun invokeReturnsFlowFromTransportManager() = runTest {
        // Arrange
        val mockEvent = mock(ForwardEvent::class.java)
        val expectedFlow: Flow<ForwardEvent> = flowOf(mockEvent)
        
        `when`(mockTransportManager.observeForwardEvents()).thenReturn(expectedFlow)

        // Act
        val result = useCase()

        // Assert
        verify(mockTransportManager).observeForwardEvents()
        assertEquals(expectedFlow, result)
    }

    @Test
    @DisplayName("invoke propagates exceptions from transportManager")
    fun invokePropagatesExceptionsFromTransportManager() = runTest {
        // Arrange
        val expectedException = RuntimeException("Forward events error")
        
        `when`(mockTransportManager.observeForwardEvents()).thenThrow(expectedException)

        // Act & Assert
        val exception = assertThrows(RuntimeException::class.java) {
            useCase()
        }
        assertEquals("Forward events error", exception.message)
    }

    @Test
    @DisplayName("invoke calls transportManager exactly once")
    fun invokeCallsTransportManagerExactlyOnce() = runTest {
        // Arrange
        val mockEvent = mock(ForwardEvent::class.java)
        val expectedFlow: Flow<ForwardEvent> = flowOf(mockEvent)
        
        `when`(mockTransportManager.observeForwardEvents()).thenReturn(expectedFlow)

        // Act
        useCase()

        // Assert
        verify(mockTransportManager, times(1)).observeForwardEvents()
    }
} 