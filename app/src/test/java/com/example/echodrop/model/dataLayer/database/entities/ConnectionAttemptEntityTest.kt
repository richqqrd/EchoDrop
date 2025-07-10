package com.example.echodrop.model.dataLayer.database.entities

import com.example.echodrop.model.dataLayer.datasource.persistence.entities.ConnectionAttemptEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `ConnectionAttemptEntity` data class.
 */
class ConnectionAttemptEntityTest {

    @Nested
    @DisplayName("ConnectionAttemptEntity Construction Tests")
    inner class ConnectionAttemptEntityConstructionTest {

        @Test
        @DisplayName("Creates a ConnectionAttemptEntity with correct properties")
        fun createConnectionAttemptEntityWithCorrectProperties() {
            // Arrange & Act
            val deviceAddress = "192.168.1.100"
            val paketId = "test-paket-123"
            val timestamp = 1234567890L
            val successful = true

            val entity = ConnectionAttemptEntity(
                deviceAddress = deviceAddress,
                paketId = paketId,
                timestamp = timestamp,
                successful = successful
            )

            // Assert
            assertEquals(deviceAddress, entity.deviceAddress)
            assertEquals(paketId, entity.paketId)
            assertEquals(timestamp, entity.timestamp)
            assertTrue(entity.successful)
        }

        @Test
        @DisplayName("Creates a failed ConnectionAttemptEntity")
        fun createFailedConnectionAttemptEntity() {
            // Arrange & Act
            val entity = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.101",
                paketId = "failed-paket-456",
                timestamp = 9876543210L,
                successful = false
            )

            // Assert
            assertEquals("192.168.1.101", entity.deviceAddress)
            assertEquals("failed-paket-456", entity.paketId)
            assertEquals(9876543210L, entity.timestamp)
            assertFalse(entity.successful)
        }

        @Test
        @DisplayName("Creates entity with current timestamp")
        fun createEntityWithCurrentTimestamp() {
            // Arrange
            val currentTime = System.currentTimeMillis()

            // Act
            val entity = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = currentTime,
                successful = true
            )

            // Assert
            assertEquals(currentTime, entity.timestamp)
            assertTrue(entity.timestamp > 0)
        }
    }

    @Nested
    @DisplayName("ConnectionAttemptEntity Equality Tests")
    inner class ConnectionAttemptEntityEqualityTest {

        @Test
        @DisplayName("Two identical ConnectionAttemptEntities are equal")
        fun twoIdenticalConnectionAttemptEntitiesAreEqual() {
            // Arrange
            val entity1 = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )

            val entity2 = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )

            // Assert
            assertEquals(entity1, entity2)
            assertEquals(entity1.hashCode(), entity2.hashCode())
        }

        @Test
        @DisplayName("ConnectionAttemptEntities with different properties are not equal")
        fun connectionAttemptEntitiesWithDifferentPropertiesAreNotEqual() {
            // Arrange
            val baseEntity = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )

            val differentDevice = baseEntity.copy(deviceAddress = "192.168.1.101")
            val differentPaket = baseEntity.copy(paketId = "paket-456")
            val differentTimestamp = baseEntity.copy(timestamp = 9876543210L)
            val differentSuccess = baseEntity.copy(successful = false)

            // Assert
            assertNotEquals(baseEntity, differentDevice)
            assertNotEquals(baseEntity, differentPaket)
            assertNotEquals(baseEntity, differentTimestamp)
            assertNotEquals(baseEntity, differentSuccess)
        }

        @Test
        @DisplayName("Entities with same composite key but different success are not equal")
        fun entitiesWithSameCompositeKeyButDifferentSuccessAreNotEqual() {
            // Arrange
            val entity1 = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )

            val entity2 = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = false
            )

            // Assert
            assertNotEquals(entity1, entity2)
        }
    }

    @Nested
    @DisplayName("ConnectionAttemptEntity Copy Tests")
    inner class ConnectionAttemptEntityCopyTest {

        @Test
        @DisplayName("Copy with modified success status creates correct instance")
        fun copyWithModifiedSuccessStatusCreatesCorrectInstance() {
            // Arrange
            val original = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )

            // Act
            val copied = original.copy(successful = false)

            // Assert
            assertEquals(original.deviceAddress, copied.deviceAddress)
            assertEquals(original.paketId, copied.paketId)
            assertEquals(original.timestamp, copied.timestamp)
            assertFalse(copied.successful)
            assertNotEquals(original, copied)
        }

        @Test
        @DisplayName("Copy with modified timestamp creates correct instance")
        fun copyWithModifiedTimestampCreatesCorrectInstance() {
            // Arrange
            val original = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )
            val newTimestamp = 9999999999L

            // Act
            val copied = original.copy(timestamp = newTimestamp)

            // Assert
            assertEquals(original.deviceAddress, copied.deviceAddress)
            assertEquals(original.paketId, copied.paketId)
            assertEquals(newTimestamp, copied.timestamp)
            assertEquals(original.successful, copied.successful)
        }

        @Test
        @DisplayName("Copy with modified device address creates correct instance")
        fun copyWithModifiedDeviceAddressCreatesCorrectInstance() {
            // Arrange
            val original = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )
            val newDeviceAddress = "192.168.1.200"

            // Act
            val copied = original.copy(deviceAddress = newDeviceAddress)

            // Assert
            assertEquals(newDeviceAddress, copied.deviceAddress)
            assertEquals(original.paketId, copied.paketId)
            assertEquals(original.timestamp, copied.timestamp)
            assertEquals(original.successful, copied.successful)
        }
    }

    @Nested
    @DisplayName("ConnectionAttemptEntity Business Logic Tests")
    inner class ConnectionAttemptEntityBusinessLogicTest {

        @Test
        @DisplayName("Entity represents successful connection attempt")
        fun entityRepresentsSuccessfulConnectionAttempt() {
            // Arrange & Act
            val successfulAttempt = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = System.currentTimeMillis(),
                successful = true
            )

            // Assert
            assertTrue(successfulAttempt.successful)
            assertNotNull(successfulAttempt.deviceAddress)
            assertNotNull(successfulAttempt.paketId)
            assertTrue(successfulAttempt.timestamp > 0)
        }

        @Test
        @DisplayName("Entity represents failed connection attempt")
        fun entityRepresentsFailedConnectionAttempt() {
            // Arrange & Act
            val failedAttempt = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = System.currentTimeMillis(),
                successful = false
            )

            // Assert
            assertFalse(failedAttempt.successful)
            assertNotNull(failedAttempt.deviceAddress)
            assertNotNull(failedAttempt.paketId)
            assertTrue(failedAttempt.timestamp > 0)
        }

        @Test
        @DisplayName("Entity supports different device address formats")
        fun entitySupportsDifferentDeviceAddressFormats() {
            // Arrange & Act
            val ipAddress = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )

            val macAddress = ConnectionAttemptEntity(
                deviceAddress = "AA:BB:CC:DD:EE:FF",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = true
            )

            // Assert
            assertEquals("192.168.1.100", ipAddress.deviceAddress)
            assertEquals("AA:BB:CC:DD:EE:FF", macAddress.deviceAddress)
            assertNotEquals(ipAddress, macAddress)
        }

        @Test
        @DisplayName("Entity can track multiple attempts for same paket")
        fun entityCanTrackMultipleAttemptsForSamePaket() {
            // Arrange & Act
            val attempt1 = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567890L,
                successful = false
            )

            val attempt2 = ConnectionAttemptEntity(
                deviceAddress = "192.168.1.100",
                paketId = "paket-123",
                timestamp = 1234567900L, // 10 seconds later
                successful = true
            )

            // Assert
            assertEquals(attempt1.deviceAddress, attempt2.deviceAddress)
            assertEquals(attempt1.paketId, attempt2.paketId)
            assertNotEquals(attempt1.timestamp, attempt2.timestamp)
            assertNotEquals(attempt1.successful, attempt2.successful)
            assertNotEquals(attempt1, attempt2) // Different due to timestamp and success
        }
    }
} 