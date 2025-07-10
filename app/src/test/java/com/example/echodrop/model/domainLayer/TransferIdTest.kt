package com.example.echodrop.model.domainLayer

import com.example.echodrop.model.domainLayer.model.TransferId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `TransferId` value class.
 */
class TransferIdTest {

    @Nested
    @DisplayName("TransferId Construction Tests")
    inner class TransferIdConstructionTest {

        @Test
        @DisplayName("Creates a TransferId with the correct value")
        fun createTransferIdWithCorrectValue() {
            // Arrange & Act
            val id = "transfer-123-456"
            val transferId = TransferId(id)

            // Assert
            assertEquals(id, transferId.value)
        }

        @Test
        @DisplayName("Creates a TransferId with UUID-like value")
        fun createTransferIdWithUuidLikeValue() {
            // Arrange & Act
            val id = "550e8400-e29b-41d4-a716-446655440000"
            val transferId = TransferId(id)

            // Assert
            assertEquals(id, transferId.value)
        }

        @Test
        @DisplayName("Creates a TransferId with simple numeric value")
        fun createTransferIdWithSimpleNumericValue() {
            // Arrange & Act
            val id = "12345"
            val transferId = TransferId(id)

            // Assert
            assertEquals(id, transferId.value)
        }

        @Test
        @DisplayName("Creates a TransferId with empty string")
        fun createTransferIdWithEmptyString() {
            // Arrange & Act
            val id = ""
            val transferId = TransferId(id)

            // Assert
            assertEquals(id, transferId.value)
        }

        @Test
        @DisplayName("Creates a TransferId with special characters")
        fun createTransferIdWithSpecialCharacters() {
            // Arrange & Act
            val id = "transfer_id-2024@device#1"
            val transferId = TransferId(id)

            // Assert
            assertEquals(id, transferId.value)
        }
    }

    @Nested
    @DisplayName("TransferId Equality Tests")
    inner class TransferIdEqualityTest {

        @Test
        @DisplayName("Two TransferIds with the same value are equal")
        fun twoIdenticalTransferIdsAreEqual() {
            // Arrange & Act
            val id1 = TransferId("transfer-123")
            val id2 = TransferId("transfer-123")

            // Assert
            assertEquals(id1, id2)
            assertEquals(id1.hashCode(), id2.hashCode())
        }

        @Test
        @DisplayName("Two TransferIds with different values are not equal")
        fun twoDifferentTransferIdsAreNotEqual() {
            // Arrange & Act
            val id1 = TransferId("transfer-123")
            val id2 = TransferId("transfer-456")

            // Assert
            assertNotEquals(id1, id2)
            assertNotEquals(id1.hashCode(), id2.hashCode())
        }

        @Test
        @DisplayName("TransferId equals itself")
        fun transferIdEqualsItself() {
            // Arrange & Act
            val transferId = TransferId("transfer-123")

            // Assert
            assertEquals(transferId, transferId)
        }

        @Test
        @DisplayName("TransferId with empty string equals another with empty string")
        fun transferIdWithEmptyStringEqualsAnotherWithEmptyString() {
            // Arrange & Act
            val id1 = TransferId("")
            val id2 = TransferId("")

            // Assert
            assertEquals(id1, id2)
            assertEquals(id1.hashCode(), id2.hashCode())
        }

        @Test
        @DisplayName("Case sensitive equality")
        fun caseSensitiveEquality() {
            // Arrange & Act
            val id1 = TransferId("Transfer-123")
            val id2 = TransferId("transfer-123")

            // Assert
            assertNotEquals(id1, id2)
        }
    }

    @Nested
    @DisplayName("TransferId String Representation Tests")
    inner class TransferIdStringRepresentationTest {

        @Test
        @DisplayName("toString returns TransferId(value=x) format by default")
        fun toStringReturnsStandardFormat() {
            // Arrange
            val idValue = "transfer-123-456"
            val transferId = TransferId(idValue)

            // Act & Assert
            assertEquals("TransferId(value=$idValue)", transferId.toString())
        }

        @Test
        @DisplayName("toString works with empty string value")
        fun toStringWorksWithEmptyStringValue() {
            // Arrange
            val transferId = TransferId("")

            // Act & Assert
            assertEquals("TransferId(value=)", transferId.toString())
        }

        @Test
        @DisplayName("toString works with UUID-like value")
        fun toStringWorksWithUuidLikeValue() {
            // Arrange
            val idValue = "550e8400-e29b-41d4-a716-446655440000"
            val transferId = TransferId(idValue)

            // Act & Assert
            assertEquals("TransferId(value=$idValue)", transferId.toString())
        }
    }

    @Nested
    @DisplayName("TransferId Value Access Tests")
    inner class TransferIdValueAccessTest {

        @Test
        @DisplayName("Value property returns original string")
        fun valuePropertyReturnsOriginalString() {
            // Arrange
            val originalValue = "my-transfer-id-2024"
            val transferId = TransferId(originalValue)

            // Act
            val retrievedValue = transferId.value

            // Assert
            assertEquals(originalValue, retrievedValue)
        }

        @Test
        @DisplayName("Value is immutable")
        fun valueIsImmutable() {
            // Arrange
            val originalValue = "transfer-id-123"
            val transferId = TransferId(originalValue)

            // Act
            val value1 = transferId.value
            val value2 = transferId.value

            // Assert
            assertEquals(value1, value2)
            assertEquals(originalValue, value1)
            assertEquals(originalValue, value2)
        }
    }

    @Nested
    @DisplayName("TransferId Collection Tests")
    inner class TransferIdCollectionTest {

        @Test
        @DisplayName("Can be used in lists")
        fun canBeUsedInLists() {
            // Arrange
            val id1 = TransferId("transfer-1")
            val id2 = TransferId("transfer-2")
            val id3 = TransferId("transfer-3")

            // Act
            val transferIds = listOf(id1, id2, id3)

            // Assert
            assertEquals(3, transferIds.size)
            assertTrue(transferIds.contains(id1))
            assertTrue(transferIds.contains(id2))
            assertTrue(transferIds.contains(id3))
        }

        @Test
        @DisplayName("Can be used in sets")
        fun canBeUsedInSets() {
            // Arrange
            val id1 = TransferId("transfer-1")
            val id2 = TransferId("transfer-2")
            val id3 = TransferId("transfer-1") // duplicate

            // Act
            val transferIdSet = setOf(id1, id2, id3)

            // Assert
            assertEquals(2, transferIdSet.size) // duplicates removed
            assertTrue(transferIdSet.contains(id1))
            assertTrue(transferIdSet.contains(id2))
        }

        @Test
        @DisplayName("Can be used as map keys")
        fun canBeUsedAsMapKeys() {
            // Arrange
            val transferId1 = TransferId("transfer-1")
            val transferId2 = TransferId("transfer-2")

            // Act
            val transferMap = mapOf(
                transferId1 to "First Transfer",
                transferId2 to "Second Transfer"
            )

            // Assert
            assertEquals("First Transfer", transferMap[transferId1])
            assertEquals("Second Transfer", transferMap[transferId2])
            assertEquals(2, transferMap.size)
        }
    }

    @Nested
    @DisplayName("TransferId Business Logic Tests")
    inner class TransferIdBusinessLogicTest {

        @Test
        @DisplayName("Can represent different transfer types")
        fun canRepresentDifferentTransferTypes() {
            // Arrange & Act
            val outgoingTransfer = TransferId("outgoing-transfer-123")
            val incomingTransfer = TransferId("incoming-transfer-456")
            val pausedTransfer = TransferId("paused-transfer-789")

            // Assert
            assertTrue(outgoingTransfer.value.contains("outgoing"))
            assertTrue(incomingTransfer.value.contains("incoming"))
            assertTrue(pausedTransfer.value.contains("paused"))
        }

        @Test
        @DisplayName("Can be used in when expressions")
        fun canBeUsedInWhenExpressions() {
            // Arrange
            val transferId = TransferId("test-transfer-123")

            // Act
            val result = when (transferId.value) {
                "test-transfer-123" -> "Found test transfer"
                "another-transfer" -> "Found another transfer"
                else -> "Unknown transfer"
            }

            // Assert
            assertEquals("Found test transfer", result)
        }

        @Test
        @DisplayName("Supports copying pattern via constructor")
        fun supportsCopyingPatternViaConstructor() {
            // Arrange
            val original = TransferId("original-transfer-123")

            // Act
            val copied = TransferId(original.value)

            // Assert
            assertEquals(original, copied)
            assertEquals(original.value, copied.value)
            assertNotSame(original, copied) // Different instances
        }
    }
} 