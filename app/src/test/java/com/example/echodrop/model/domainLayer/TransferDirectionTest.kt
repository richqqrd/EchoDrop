package com.example.echodrop.model.domainLayer

import com.example.echodrop.model.domainLayer.model.TransferDirection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `TransferDirection` enum.
 */
class TransferDirectionTest {

    @Nested
    @DisplayName("TransferDirection Enum Values Tests")
    inner class TransferDirectionEnumValuesTest {

        @Test
        @DisplayName("TransferDirection has exactly two values")
        fun transferDirectionHasExactlyTwoValues() {
            // Act
            val values = TransferDirection.values()

            // Assert
            assertEquals(2, values.size)
        }

        @Test
        @DisplayName("TransferDirection contains OUTGOING value")
        fun transferDirectionContainsOutgoingValue() {
            // Act
            val values = TransferDirection.values()

            // Assert
            assertTrue(values.contains(TransferDirection.OUTGOING))
        }

        @Test
        @DisplayName("TransferDirection contains INCOMING value")
        fun transferDirectionContainsIncomingValue() {
            // Act
            val values = TransferDirection.values()

            // Assert
            assertTrue(values.contains(TransferDirection.INCOMING))
        }

        @Test
        @DisplayName("TransferDirection values are ordered correctly")
        fun transferDirectionValuesAreOrderedCorrectly() {
            // Act
            val values = TransferDirection.values()

            // Assert
            assertEquals(TransferDirection.OUTGOING, values[0])
            assertEquals(TransferDirection.INCOMING, values[1])
        }
    }

    @Nested
    @DisplayName("TransferDirection valueOf Tests")
    inner class TransferDirectionValueOfTest {

        @Test
        @DisplayName("valueOf returns OUTGOING for 'OUTGOING' string")
        fun valueOfReturnsOutgoingForOutgoingString() {
            // Act
            val result = TransferDirection.valueOf("OUTGOING")

            // Assert
            assertEquals(TransferDirection.OUTGOING, result)
        }

        @Test
        @DisplayName("valueOf returns INCOMING for 'INCOMING' string")
        fun valueOfReturnsIncomingForIncomingString() {
            // Act
            val result = TransferDirection.valueOf("INCOMING")

            // Assert
            assertEquals(TransferDirection.INCOMING, result)
        }

        @Test
        @DisplayName("valueOf throws exception for invalid string")
        fun valueOfThrowsExceptionForInvalidString() {
            // Act & Assert
            assertThrows(IllegalArgumentException::class.java) {
                TransferDirection.valueOf("INVALID")
            }
        }

        @Test
        @DisplayName("valueOf is case sensitive")
        fun valueOfIsCaseSensitive() {
            // Act & Assert
            assertThrows(IllegalArgumentException::class.java) {
                TransferDirection.valueOf("outgoing")
            }
            
            assertThrows(IllegalArgumentException::class.java) {
                TransferDirection.valueOf("incoming")
            }
        }
    }

    @Nested
    @DisplayName("TransferDirection String Representation Tests")
    inner class TransferDirectionStringRepresentationTest {

        @Test
        @DisplayName("OUTGOING name returns 'OUTGOING'")
        fun outgoingNameReturnsOutgoing() {
            // Act
            val name = TransferDirection.OUTGOING.name

            // Assert
            assertEquals("OUTGOING", name)
        }

        @Test
        @DisplayName("INCOMING name returns 'INCOMING'")
        fun incomingNameReturnsIncoming() {
            // Act
            val name = TransferDirection.INCOMING.name

            // Assert
            assertEquals("INCOMING", name)
        }

        @Test
        @DisplayName("OUTGOING toString returns 'OUTGOING'")
        fun outgoingToStringReturnsOutgoing() {
            // Act
            val string = TransferDirection.OUTGOING.toString()

            // Assert
            assertEquals("OUTGOING", string)
        }

        @Test
        @DisplayName("INCOMING toString returns 'INCOMING'")
        fun incomingToStringReturnsIncoming() {
            // Act
            val string = TransferDirection.INCOMING.toString()

            // Assert
            assertEquals("INCOMING", string)
        }
    }

    @Nested
    @DisplayName("TransferDirection Ordinal Tests")
    inner class TransferDirectionOrdinalTest {

        @Test
        @DisplayName("OUTGOING has ordinal 0")
        fun outgoingHasOrdinalZero() {
            // Act
            val ordinal = TransferDirection.OUTGOING.ordinal

            // Assert
            assertEquals(0, ordinal)
        }

        @Test
        @DisplayName("INCOMING has ordinal 1")
        fun incomingHasOrdinalOne() {
            // Act
            val ordinal = TransferDirection.INCOMING.ordinal

            // Assert
            assertEquals(1, ordinal)
        }
    }

    @Nested
    @DisplayName("TransferDirection Equality Tests")
    inner class TransferDirectionEqualityTest {

        @Test
        @DisplayName("OUTGOING equals itself")
        fun outgoingEqualsItself() {
            // Assert
            assertEquals(TransferDirection.OUTGOING, TransferDirection.OUTGOING)
        }

        @Test
        @DisplayName("INCOMING equals itself")
        fun incomingEqualsItself() {
            // Assert
            assertEquals(TransferDirection.INCOMING, TransferDirection.INCOMING)
        }

        @Test
        @DisplayName("OUTGOING does not equal INCOMING")
        fun outgoingDoesNotEqualIncoming() {
            // Assert
            assertNotEquals(TransferDirection.OUTGOING, TransferDirection.INCOMING)
        }

        @Test
        @DisplayName("INCOMING does not equal OUTGOING")
        fun incomingDoesNotEqualOutgoing() {
            // Assert
            assertNotEquals(TransferDirection.INCOMING, TransferDirection.OUTGOING)
        }
    }

    @Nested
    @DisplayName("TransferDirection Business Logic Tests")
    inner class TransferDirectionBusinessLogicTest {

        @Test
        @DisplayName("Can be used in when expression")
        fun canBeUsedInWhenExpression() {
            // Arrange
            val outgoingDirection = TransferDirection.OUTGOING
            val incomingDirection = TransferDirection.INCOMING

            // Act
            val outgoingResult = when (outgoingDirection) {
                TransferDirection.OUTGOING -> "Sending"
                TransferDirection.INCOMING -> "Receiving"
            }

            val incomingResult = when (incomingDirection) {
                TransferDirection.OUTGOING -> "Sending"
                TransferDirection.INCOMING -> "Receiving"
            }

            // Assert
            assertEquals("Sending", outgoingResult)
            assertEquals("Receiving", incomingResult)
        }

        @Test
        @DisplayName("Can be used in collections")
        fun canBeUsedInCollections() {
            // Arrange
            val directions = listOf(TransferDirection.OUTGOING, TransferDirection.INCOMING)

            // Assert
            assertTrue(directions.contains(TransferDirection.OUTGOING))
            assertTrue(directions.contains(TransferDirection.INCOMING))
            assertEquals(2, directions.size)
        }

        @Test
        @DisplayName("Can be used as map keys")
        fun canBeUsedAsMapKeys() {
            // Arrange
            val directionDescriptions = mapOf(
                TransferDirection.OUTGOING to "Sending to peer",
                TransferDirection.INCOMING to "Receiving from peer"
            )

            // Assert
            assertEquals("Sending to peer", directionDescriptions[TransferDirection.OUTGOING])
            assertEquals("Receiving from peer", directionDescriptions[TransferDirection.INCOMING])
        }

        @Test
        @DisplayName("Opposite direction logic")
        fun oppositeDirectionLogic() {
            // Act & Assert
            val opposite = when (TransferDirection.OUTGOING) {
                TransferDirection.OUTGOING -> TransferDirection.INCOMING
                TransferDirection.INCOMING -> TransferDirection.OUTGOING
            }
            assertEquals(TransferDirection.INCOMING, opposite)

            val oppositeIncoming = when (TransferDirection.INCOMING) {
                TransferDirection.OUTGOING -> TransferDirection.INCOMING
                TransferDirection.INCOMING -> TransferDirection.OUTGOING
            }
            assertEquals(TransferDirection.OUTGOING, oppositeIncoming)
        }
    }
} 