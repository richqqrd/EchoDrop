package com.example.echodrop.model.domainLayer

import com.example.echodrop.model.domainLayer.model.DeviceInfo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `DeviceInfo` data class.
 */
class DeviceInfoTest {

    @Nested
    @DisplayName("DeviceInfo Construction Tests")
    inner class DeviceInfoConstructionTest {

        @Test
        @DisplayName("Creates a DeviceInfo with device name and address")
        fun createDeviceInfoWithDeviceNameAndAddress() {
            // Arrange
            val deviceName = "Samsung Galaxy S21"
            val deviceAddress = "AA:BB:CC:DD:EE:FF"

            // Act
            val deviceInfo = DeviceInfo(
                deviceName = deviceName,
                deviceAddress = deviceAddress
            )

            // Assert
            assertEquals(deviceName, deviceInfo.deviceName)
            assertEquals(deviceAddress, deviceInfo.deviceAddress)
        }

        @Test
        @DisplayName("Creates a DeviceInfo with null device name")
        fun createDeviceInfoWithNullDeviceName() {
            // Arrange
            val deviceName: String? = null
            val deviceAddress = "AA:BB:CC:DD:EE:FF"

            // Act
            val deviceInfo = DeviceInfo(
                deviceName = deviceName,
                deviceAddress = deviceAddress
            )

            // Assert
            assertNull(deviceInfo.deviceName)
            assertEquals(deviceAddress, deviceInfo.deviceAddress)
        }

        @Test
        @DisplayName("Creates a DeviceInfo with empty device name")
        fun createDeviceInfoWithEmptyDeviceName() {
            // Arrange
            val deviceName = ""
            val deviceAddress = "AA:BB:CC:DD:EE:FF"

            // Act
            val deviceInfo = DeviceInfo(
                deviceName = deviceName,
                deviceAddress = deviceAddress
            )

            // Assert
            assertEquals("", deviceInfo.deviceName)
            assertEquals(deviceAddress, deviceInfo.deviceAddress)
        }

        @Test
        @DisplayName("Creates a DeviceInfo with long device name")
        fun createDeviceInfoWithLongDeviceName() {
            // Arrange
            val deviceName = "Very Long Device Name That Might Be Used In Real World Scenarios"
            val deviceAddress = "AA:BB:CC:DD:EE:FF"

            // Act
            val deviceInfo = DeviceInfo(
                deviceName = deviceName,
                deviceAddress = deviceAddress
            )

            // Assert
            assertEquals(deviceName, deviceInfo.deviceName)
            assertEquals(deviceAddress, deviceInfo.deviceAddress)
        }
    }

    @Nested
    @DisplayName("DeviceInfo Equality Tests")
    inner class DeviceInfoEqualityTest {

        @Test
        @DisplayName("Two DeviceInfos with identical values are equal")
        fun twoIdenticalDeviceInfosAreEqual() {
            // Arrange
            val deviceName = "iPhone 13"
            val deviceAddress = "11:22:33:44:55:66"

            // Act
            val deviceInfo1 = DeviceInfo(deviceName, deviceAddress)
            val deviceInfo2 = DeviceInfo(deviceName, deviceAddress)

            // Assert
            assertEquals(deviceInfo1, deviceInfo2)
            assertEquals(deviceInfo1.hashCode(), deviceInfo2.hashCode())
        }

        @Test
        @DisplayName("Two DeviceInfos with different names are not equal")
        fun deviceInfosWithDifferentNamesAreNotEqual() {
            // Arrange
            val deviceAddress = "11:22:33:44:55:66"
            val deviceInfo1 = DeviceInfo("iPhone 13", deviceAddress)
            val deviceInfo2 = DeviceInfo("Samsung Galaxy", deviceAddress)

            // Assert
            assertNotEquals(deviceInfo1, deviceInfo2)
        }

        @Test
        @DisplayName("Two DeviceInfos with different addresses are not equal")
        fun deviceInfosWithDifferentAddressesAreNotEqual() {
            // Arrange
            val deviceName = "iPhone 13"
            val deviceInfo1 = DeviceInfo(deviceName, "11:22:33:44:55:66")
            val deviceInfo2 = DeviceInfo(deviceName, "AA:BB:CC:DD:EE:FF")

            // Assert
            assertNotEquals(deviceInfo1, deviceInfo2)
        }

        @Test
        @DisplayName("DeviceInfo with null name equals another with null name")
        fun deviceInfoWithNullNameEqualsAnotherWithNullName() {
            // Arrange
            val deviceAddress = "11:22:33:44:55:66"
            val deviceInfo1 = DeviceInfo(null, deviceAddress)
            val deviceInfo2 = DeviceInfo(null, deviceAddress)

            // Assert
            assertEquals(deviceInfo1, deviceInfo2)
            assertEquals(deviceInfo1.hashCode(), deviceInfo2.hashCode())
        }

        @Test
        @DisplayName("DeviceInfo with null name not equal to one with actual name")
        fun deviceInfoWithNullNameNotEqualToOneWithActualName() {
            // Arrange
            val deviceAddress = "11:22:33:44:55:66"
            val deviceInfo1 = DeviceInfo(null, deviceAddress)
            val deviceInfo2 = DeviceInfo("iPhone 13", deviceAddress)

            // Assert
            assertNotEquals(deviceInfo1, deviceInfo2)
        }
    }

    @Nested
    @DisplayName("DeviceInfo Copy Tests")
    inner class DeviceInfoCopyTest {

        @Test
        @DisplayName("Copy with modified device name creates correct instance")
        fun copyWithModifiedDeviceName() {
            // Arrange
            val original = DeviceInfo("Original Name", "11:22:33:44:55:66")
            val newName = "Updated Name"

            // Act
            val copied = original.copy(deviceName = newName)

            // Assert
            assertEquals(newName, copied.deviceName)
            assertEquals(original.deviceAddress, copied.deviceAddress)
            assertNotEquals(original, copied)
        }

        @Test
        @DisplayName("Copy with modified device address creates correct instance")
        fun copyWithModifiedDeviceAddress() {
            // Arrange
            val original = DeviceInfo("iPhone 13", "11:22:33:44:55:66")
            val newAddress = "AA:BB:CC:DD:EE:FF"

            // Act
            val copied = original.copy(deviceAddress = newAddress)

            // Assert
            assertEquals(original.deviceName, copied.deviceName)
            assertEquals(newAddress, copied.deviceAddress)
            assertNotEquals(original, copied)
        }

        @Test
        @DisplayName("Copy with both values modified creates correct instance")
        fun copyWithBothValuesModified() {
            // Arrange
            val original = DeviceInfo("iPhone 13", "11:22:33:44:55:66")
            val newName = "Samsung Galaxy"
            val newAddress = "AA:BB:CC:DD:EE:FF"

            // Act
            val copied = original.copy(deviceName = newName, deviceAddress = newAddress)

            // Assert
            assertEquals(newName, copied.deviceName)
            assertEquals(newAddress, copied.deviceAddress)
            assertNotEquals(original, copied)
        }

        @Test
        @DisplayName("Copy with no changes creates identical instance")
        fun copyWithNoChangesCreatesIdenticalInstance() {
            // Arrange
            val original = DeviceInfo("iPhone 13", "11:22:33:44:55:66")

            // Act
            val copied = original.copy()

            // Assert
            assertEquals(original, copied)
            assertEquals(original.deviceName, copied.deviceName)
            assertEquals(original.deviceAddress, copied.deviceAddress)
        }
    }

    @Nested
    @DisplayName("DeviceInfo String Representation Tests")
    inner class DeviceInfoStringRepresentationTest {

        @Test
        @DisplayName("toString returns correct format with device name")
        fun toStringReturnsCorrectFormatWithDeviceName() {
            // Arrange
            val deviceName = "iPhone 13"
            val deviceAddress = "11:22:33:44:55:66"
            val deviceInfo = DeviceInfo(deviceName, deviceAddress)

            // Act
            val result = deviceInfo.toString()

            // Assert
            assertEquals("DeviceInfo(deviceName=$deviceName, deviceAddress=$deviceAddress)", result)
        }

        @Test
        @DisplayName("toString returns correct format with null device name")
        fun toStringReturnsCorrectFormatWithNullDeviceName() {
            // Arrange
            val deviceAddress = "11:22:33:44:55:66"
            val deviceInfo = DeviceInfo(null, deviceAddress)

            // Act
            val result = deviceInfo.toString()

            // Assert
            assertEquals("DeviceInfo(deviceName=null, deviceAddress=$deviceAddress)", result)
        }
    }

    @Nested
    @DisplayName("DeviceInfo Business Logic Tests")
    inner class DeviceInfoBusinessLogicTest {

        @Test
        @DisplayName("Device address is always required and cannot be empty")
        fun deviceAddressIsAlwaysRequiredAndCannotBeEmpty() {
            // Arrange
            val deviceInfo = DeviceInfo("Test Device", "AA:BB:CC:DD:EE:FF")

            // Assert
            assertNotNull(deviceInfo.deviceAddress)
            assertFalse(deviceInfo.deviceAddress.isEmpty())
        }

        @Test
        @DisplayName("Device name can be optional for unknown devices")
        fun deviceNameCanBeOptionalForUnknownDevices() {
            // Arrange
            val unknownDevice = DeviceInfo(null, "AA:BB:CC:DD:EE:FF")

            // Assert
            assertNull(unknownDevice.deviceName)
            assertNotNull(unknownDevice.deviceAddress)
        }

        @Test
        @DisplayName("Device with valid MAC address format")
        fun deviceWithValidMacAddressFormat() {
            // Arrange
            val macAddress = "AA:BB:CC:DD:EE:FF"
            val deviceInfo = DeviceInfo("Test Device", macAddress)

            // Assert
            assertEquals(macAddress, deviceInfo.deviceAddress)
            assertTrue(deviceInfo.deviceAddress.contains(":"))
            assertEquals(17, deviceInfo.deviceAddress.length) // MAC address length
        }
    }
} 