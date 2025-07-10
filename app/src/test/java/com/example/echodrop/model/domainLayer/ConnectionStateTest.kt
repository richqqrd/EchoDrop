package com.example.echodrop.model.domainLayer

import com.example.echodrop.model.domainLayer.model.ConnectionState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `ConnectionState` data class.
 */
class ConnectionStateTest {

    @Nested
    @DisplayName("ConnectionState Construction Tests")
    inner class ConnectionStateConstructionTest {

        @Test
        @DisplayName("Creates a ConnectionState with default values")
        fun createConnectionStateWithDefaultValues() {
            // Act
            val connectionState = ConnectionState()

            // Assert
            assertFalse(connectionState.isConnected)
            assertTrue(connectionState.connectedDevices.isEmpty())
            assertNull(connectionState.groupOwnerAddress)
            assertFalse(connectionState.isGroupOwner)
        }

        @Test
        @DisplayName("Creates a ConnectionState with custom values")
        fun createConnectionStateWithCustomValues() {
            // Arrange
            val isConnected = true
            val connectedDevices = setOf("device1", "device2", "device3")
            val groupOwnerAddress = "192.168.49.1"
            val isGroupOwner = true

            // Act
            val connectionState = ConnectionState(
                isConnected = isConnected,
                connectedDevices = connectedDevices,
                groupOwnerAddress = groupOwnerAddress,
                isGroupOwner = isGroupOwner
            )

            // Assert
            assertTrue(connectionState.isConnected)
            assertEquals(connectedDevices, connectionState.connectedDevices)
            assertEquals(groupOwnerAddress, connectionState.groupOwnerAddress)
            assertTrue(connectionState.isGroupOwner)
        }

        @Test
        @DisplayName("Creates a ConnectionState as group member")
        fun createConnectionStateAsGroupMember() {
            // Arrange
            val isConnected = true
            val connectedDevices = setOf("device1")
            val groupOwnerAddress = "192.168.49.1"
            val isGroupOwner = false

            // Act
            val connectionState = ConnectionState(
                isConnected = isConnected,
                connectedDevices = connectedDevices,
                groupOwnerAddress = groupOwnerAddress,
                isGroupOwner = isGroupOwner
            )

            // Assert
            assertTrue(connectionState.isConnected)
            assertEquals(connectedDevices, connectionState.connectedDevices)
            assertEquals(groupOwnerAddress, connectionState.groupOwnerAddress)
            assertFalse(connectionState.isGroupOwner)
        }
    }

    @Nested
    @DisplayName("ConnectionState Equality Tests")
    inner class ConnectionStateEqualityTest {

        @Test
        @DisplayName("Two ConnectionStates with identical values are equal")
        fun twoIdenticalConnectionStatesAreEqual() {
            // Arrange
            val connectedDevices = setOf("device1", "device2")
            val groupOwnerAddress = "192.168.49.1"

            // Act
            val state1 = ConnectionState(
                isConnected = true,
                connectedDevices = connectedDevices,
                groupOwnerAddress = groupOwnerAddress,
                isGroupOwner = true
            )
            val state2 = ConnectionState(
                isConnected = true,
                connectedDevices = connectedDevices,
                groupOwnerAddress = groupOwnerAddress,
                isGroupOwner = true
            )

            // Assert
            assertEquals(state1, state2)
            assertEquals(state1.hashCode(), state2.hashCode())
        }

        @Test
        @DisplayName("Two ConnectionStates with different connection status are not equal")
        fun connectionStatesWithDifferentConnectionStatusAreNotEqual() {
            // Arrange
            val connectedDevices = setOf("device1")

            val state1 = ConnectionState(
                isConnected = true,
                connectedDevices = connectedDevices,
                groupOwnerAddress = "192.168.49.1",
                isGroupOwner = false
            )
            val state2 = ConnectionState(
                isConnected = false,
                connectedDevices = connectedDevices,
                groupOwnerAddress = "192.168.49.1",
                isGroupOwner = false
            )

            // Assert
            assertNotEquals(state1, state2)
        }

        @Test
        @DisplayName("Two ConnectionStates with different connected devices are not equal")
        fun connectionStatesWithDifferentConnectedDevicesAreNotEqual() {
            // Arrange
            val devices1 = setOf("device1", "device2")
            val devices2 = setOf("device1", "device3")

            val state1 = ConnectionState(connectedDevices = devices1)
            val state2 = ConnectionState(connectedDevices = devices2)

            // Assert
            assertNotEquals(state1, state2)
        }
    }

    @Nested
    @DisplayName("ConnectionState Copy Tests")
    inner class ConnectionStateCopyTest {

        @Test
        @DisplayName("Copy with modified connection status creates correct instance")
        fun copyWithModifiedConnectionStatus() {
            // Arrange
            val original = ConnectionState(
                isConnected = false,
                connectedDevices = setOf("device1"),
                groupOwnerAddress = null,
                isGroupOwner = false
            )

            // Act
            val copied = original.copy(
                isConnected = true,
                groupOwnerAddress = "192.168.49.1",
                isGroupOwner = true
            )

            // Assert
            assertTrue(copied.isConnected)
            assertEquals(original.connectedDevices, copied.connectedDevices)
            assertEquals("192.168.49.1", copied.groupOwnerAddress)
            assertTrue(copied.isGroupOwner)
        }

        @Test
        @DisplayName("Copy with added connected devices creates correct instance")
        fun copyWithAddedConnectedDevices() {
            // Arrange
            val original = ConnectionState(
                isConnected = true,
                connectedDevices = setOf("device1"),
                groupOwnerAddress = "192.168.49.1",
                isGroupOwner = true
            )
            val newDevices = setOf("device1", "device2", "device3")

            // Act
            val copied = original.copy(connectedDevices = newDevices)

            // Assert
            assertEquals(original.isConnected, copied.isConnected)
            assertEquals(newDevices, copied.connectedDevices)
            assertEquals(original.groupOwnerAddress, copied.groupOwnerAddress)
            assertEquals(original.isGroupOwner, copied.isGroupOwner)
        }
    }

    @Nested
    @DisplayName("ConnectionState Business Logic Tests")
    inner class ConnectionStateBusinessLogicTest {

        @Test
        @DisplayName("Disconnected state should have empty connected devices")
        fun disconnectedStateShouldHaveEmptyConnectedDevices() {
            // Arrange & Act
            val disconnectedState = ConnectionState(
                isConnected = false,
                connectedDevices = emptySet(),
                groupOwnerAddress = null,
                isGroupOwner = false
            )

            // Assert
            assertFalse(disconnectedState.isConnected)
            assertTrue(disconnectedState.connectedDevices.isEmpty())
            assertNull(disconnectedState.groupOwnerAddress)
            assertFalse(disconnectedState.isGroupOwner)
        }

        @Test
        @DisplayName("Group owner should be connected")
        fun groupOwnerShouldBeConnected() {
            // Arrange & Act
            val groupOwnerState = ConnectionState(
                isConnected = true,
                connectedDevices = setOf("member1", "member2"),
                groupOwnerAddress = "192.168.49.1",
                isGroupOwner = true
            )

            // Assert
            assertTrue(groupOwnerState.isConnected)
            assertTrue(groupOwnerState.isGroupOwner)
            assertEquals("192.168.49.1", groupOwnerState.groupOwnerAddress)
            assertFalse(groupOwnerState.connectedDevices.isEmpty())
        }
    }
} 