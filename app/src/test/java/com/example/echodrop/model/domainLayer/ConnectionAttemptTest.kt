package com.example.echodrop.model.domainLayer

import com.example.echodrop.model.domainLayer.model.ConnectionAttempt
import com.example.echodrop.model.domainLayer.model.PaketId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `ConnectionAttempt` data class.
 */
class ConnectionAttemptTest {

    @Nested
    @DisplayName("ConnectionAttempt Construction Tests")
    inner class ConnectionAttemptConstructionTest {

        @Test
        @DisplayName("Creates a ConnectionAttempt with the correct values")
        fun createConnectionAttemptWithCorrectValues() {
            // Arrange
            val deviceAddress = "192.168.1.100"
            val paketId = PaketId("test-paket-123")
            val timestamp = 1234567890L
            val successful = true

            // Act
            val connectionAttempt = ConnectionAttempt(
                deviceAddress = deviceAddress,
                paketId = paketId,
                timestamp = timestamp,
                successful = successful
            )

            // Assert
            assertEquals(deviceAddress, connectionAttempt.deviceAddress)
            assertEquals(paketId, connectionAttempt.paketId)
            assertEquals(timestamp, connectionAttempt.timestamp)
            assertTrue(connectionAttempt.successful)
        }

        @Test
        @DisplayName("Creates a failed ConnectionAttempt")
        fun createFailedConnectionAttempt() {
            // Arrange
            val deviceAddress = "192.168.1.101"
            val paketId = PaketId("failed-paket-456")
            val timestamp = 9876543210L
            val successful = false

            // Act
            val connectionAttempt = ConnectionAttempt(
                deviceAddress = deviceAddress,
                paketId = paketId,
                timestamp = timestamp,
                successful = successful
            )

            // Assert
            assertEquals(deviceAddress, connectionAttempt.deviceAddress)
            assertEquals(paketId, connectionAttempt.paketId)
            assertEquals(timestamp, connectionAttempt.timestamp)
            assertFalse(connectionAttempt.successful)
        }
    }

    @Nested
    @DisplayName("ConnectionAttempt Equality Tests")
    inner class ConnectionAttemptEqualityTest {

        @Test
        @DisplayName("Two ConnectionAttempts with identical values are equal")
        fun twoIdenticalConnectionAttemptsAreEqual() {
            // Arrange
            val deviceAddress = "192.168.1.100"
            val paketId = PaketId("test-paket-123")
            val timestamp = 1234567890L
            val successful = true

            // Act
            val attempt1 = ConnectionAttempt(deviceAddress, paketId, timestamp, successful)
            val attempt2 = ConnectionAttempt(deviceAddress, paketId, timestamp, successful)

            // Assert
            assertEquals(attempt1, attempt2)
            assertEquals(attempt1.hashCode(), attempt2.hashCode())
        }

        @Test
        @DisplayName("Two ConnectionAttempts with different values are not equal")
        fun twoDifferentConnectionAttemptsAreNotEqual() {
            // Arrange
            val attempt1 = ConnectionAttempt("192.168.1.100", PaketId("paket-1"), 1234567890L, true)
            val attempt2 = ConnectionAttempt("192.168.1.101", PaketId("paket-2"), 9876543210L, false)

            // Assert
            assertNotEquals(attempt1, attempt2)
        }

        @Test
        @DisplayName("ConnectionAttempts with same device but different success status are not equal")
        fun connectionAttemptsWithDifferentSuccessStatusAreNotEqual() {
            // Arrange
            val deviceAddress = "192.168.1.100"
            val paketId = PaketId("test-paket-123")
            val timestamp = 1234567890L

            val attempt1 = ConnectionAttempt(deviceAddress, paketId, timestamp, true)
            val attempt2 = ConnectionAttempt(deviceAddress, paketId, timestamp, false)

            // Assert
            assertNotEquals(attempt1, attempt2)
        }
    }

    @Nested
    @DisplayName("ConnectionAttempt Copy Tests")
    inner class ConnectionAttemptCopyTest {

        @Test
        @DisplayName("Copy with modified success status creates correct instance")
        fun copyWithModifiedSuccessStatus() {
            // Arrange
            val original = ConnectionAttempt("192.168.1.100", PaketId("test-paket"), 1234567890L, true)

            // Act
            val copied = original.copy(successful = false)

            // Assert
            assertEquals(original.deviceAddress, copied.deviceAddress)
            assertEquals(original.paketId, copied.paketId)
            assertEquals(original.timestamp, copied.timestamp)
            assertNotEquals(original.successful, copied.successful)
            assertFalse(copied.successful)
        }

        @Test
        @DisplayName("Copy with modified timestamp creates correct instance")
        fun copyWithModifiedTimestamp() {
            // Arrange
            val original = ConnectionAttempt("192.168.1.100", PaketId("test-paket"), 1234567890L, true)
            val newTimestamp = 9999999999L

            // Act
            val copied = original.copy(timestamp = newTimestamp)

            // Assert
            assertEquals(original.deviceAddress, copied.deviceAddress)
            assertEquals(original.paketId, copied.paketId)
            assertEquals(newTimestamp, copied.timestamp)
            assertEquals(original.successful, copied.successful)
        }
    }
} 