package com.example.echodrop.model.dataLayer.repository

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.ConnectionAttemptDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.ConnectionAttemptEntity
import com.example.echodrop.model.dataLayer.impl.repository.ConnectionAttemptRepositoryImpl
import com.example.echodrop.model.domainLayer.model.PaketId
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test class for the `ConnectionAttemptRepositoryImpl` implementation.
 */
@DisplayName("ConnectionAttemptRepositoryImpl Tests")
class ConnectionAttemptRepositoryImplTest {

    private lateinit var mockDao: ConnectionAttemptDao
    private lateinit var repository: ConnectionAttemptRepositoryImpl

    private val testDeviceAddress = "192.168.1.100"
    private val testPaketId = PaketId("test-paket-123")

    @BeforeEach
    fun setup() {
        mockDao = mock(ConnectionAttemptDao::class.java)
        repository = ConnectionAttemptRepositoryImpl(mockDao)
    }

    @Test
    @DisplayName("trackAttempt inserts successful attempt with correct data")
    fun trackAttemptInsertsSuccessfulAttemptWithCorrectData() = runTest {
        // Arrange
        val entityCaptor = argumentCaptor<ConnectionAttemptEntity>()

        // Act
        repository.trackAttempt(testDeviceAddress, testPaketId, successful = true)

        // Assert
        verify(mockDao).insert(entityCaptor.capture())
        val capturedEntity = entityCaptor.firstValue

        assertEquals(testDeviceAddress, capturedEntity.deviceAddress)
        assertEquals(testPaketId.value, capturedEntity.paketId)
        assertTrue(capturedEntity.successful)
        assertTrue(capturedEntity.timestamp > 0)
        assertTrue(capturedEntity.timestamp <= System.currentTimeMillis())
    }

    @Test
    @DisplayName("trackAttempt inserts failed attempt with correct data")
    fun trackAttemptInsertsFailedAttemptWithCorrectData() = runTest {
        // Arrange
        val entityCaptor = argumentCaptor<ConnectionAttemptEntity>()

        // Act
        repository.trackAttempt(testDeviceAddress, testPaketId, successful = false)

        // Assert
        verify(mockDao).insert(entityCaptor.capture())
        val capturedEntity = entityCaptor.firstValue

        assertEquals(testDeviceAddress, capturedEntity.deviceAddress)
        assertEquals(testPaketId.value, capturedEntity.paketId)
        assertFalse(capturedEntity.successful)
        assertTrue(capturedEntity.timestamp > 0)
    }

    @Test
    @DisplayName("getFailedAttemptCount returns count from DAO")
    fun getFailedAttemptCountReturnsCountFromDao() = runTest {
        // Arrange
        val minTimestamp = 1234567890L
        val expectedCount = 5
        whenever(mockDao.getFailedAttemptCount(testDeviceAddress, testPaketId.value, minTimestamp))
            .thenReturn(expectedCount)

        // Act
        val result = repository.getFailedAttemptCount(testDeviceAddress, testPaketId, minTimestamp)

        // Assert
        assertEquals(expectedCount, result)
        verify(mockDao).getFailedAttemptCount(testDeviceAddress, testPaketId.value, minTimestamp)
    }

    @Test
    @DisplayName("getFailedAttemptCount returns zero when no failed attempts")
    fun getFailedAttemptCountReturnsZeroWhenNoFailedAttempts() = runTest {
        // Arrange
        val minTimestamp = 1234567890L
        whenever(mockDao.getFailedAttemptCount(testDeviceAddress, testPaketId.value, minTimestamp))
            .thenReturn(0)

        // Act
        val result = repository.getFailedAttemptCount(testDeviceAddress, testPaketId, minTimestamp)

        // Assert
        assertEquals(0, result)
    }

    @Test
    @DisplayName("cleanupOldAttempts calls DAO with correct timestamp")
    fun cleanupOldAttemptsCallsDaoWithCorrectTimestamp() = runTest {
        // Arrange
        val cutoffTimestamp = 9876543210L

        // Act
        repository.cleanupOldAttempts(cutoffTimestamp)

        // Assert
        verify(mockDao).deleteOlderThan(cutoffTimestamp)
    }

    @Test
    @DisplayName("trackAttempt handles different device addresses")
    fun trackAttemptHandlesDifferentDeviceAddresses() = runTest {
        // Arrange
        val deviceAddress1 = "192.168.1.100"
        val deviceAddress2 = "192.168.1.101"
        val entityCaptor = argumentCaptor<ConnectionAttemptEntity>()

        // Act
        repository.trackAttempt(deviceAddress1, testPaketId, true)
        repository.trackAttempt(deviceAddress2, testPaketId, false)

        // Assert
        verify(mockDao, times(2)).insert(entityCaptor.capture())
        val capturedEntities = entityCaptor.allValues

        assertEquals(deviceAddress1, capturedEntities[0].deviceAddress)
        assertEquals(deviceAddress2, capturedEntities[1].deviceAddress)
        assertTrue(capturedEntities[0].successful)
        assertFalse(capturedEntities[1].successful)
    }

    @Test
    @DisplayName("trackAttempt handles different paket IDs")
    fun trackAttemptHandlesDifferentPaketIds() = runTest {
        // Arrange
        val paketId1 = PaketId("paket-1")
        val paketId2 = PaketId("paket-2")
        val entityCaptor = argumentCaptor<ConnectionAttemptEntity>()

        // Act
        repository.trackAttempt(testDeviceAddress, paketId1, true)
        repository.trackAttempt(testDeviceAddress, paketId2, false)

        // Assert
        verify(mockDao, times(2)).insert(entityCaptor.capture())
        val capturedEntities = entityCaptor.allValues

        assertEquals(paketId1.value, capturedEntities[0].paketId)
        assertEquals(paketId2.value, capturedEntities[1].paketId)
    }
} 