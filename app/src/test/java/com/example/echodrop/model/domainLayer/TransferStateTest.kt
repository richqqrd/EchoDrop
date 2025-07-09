package com.example.echodrop.model.domainLayer

import com.example.echodrop.model.domainLayer.model.TransferState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TransferStateTest {

    @Nested
    @DisplayName("TransferState Basic Tests")
    inner class TransferStateBasicTest {

        @Test
        @DisplayName("Enum contains all expected states")
        fun enumContainsAllExpectedStates() {
            val states = TransferState.entries.toTypedArray()
            // Should contain all defined states
            assertEquals(6, states.size)
            assertTrue(states.toList().containsAll(listOf(
                TransferState.QUEUED,
                TransferState.ACTIVE,
                TransferState.PAUSED,
                TransferState.DONE,
                TransferState.FAILED,
                TransferState.CANCELLED
            )))
        }

        @Test
        @DisplayName("valueOf returns correct enum value for string")
        fun valueOfReturnsCorrectEnum() {
            assertEquals(TransferState.QUEUED, TransferState.valueOf("QUEUED"))
            assertEquals(TransferState.ACTIVE, TransferState.valueOf("ACTIVE"))
            assertEquals(TransferState.DONE, TransferState.valueOf("DONE"))
            assertEquals(TransferState.FAILED, TransferState.valueOf("FAILED"))
        }

        @Test
        @DisplayName("name returns correct string representation")
        fun nameReturnsCorrectString() {
            assertEquals("QUEUED", TransferState.QUEUED.name)
            assertEquals("ACTIVE", TransferState.ACTIVE.name)
            assertEquals("DONE", TransferState.DONE.name)
            assertEquals("FAILED", TransferState.FAILED.name)
            assertEquals("PAUSED", TransferState.PAUSED.name)
            assertEquals("CANCELLED", TransferState.CANCELLED.name)
        }
    }

    @Nested
    @DisplayName("TransferState Comparison Tests")
    inner class TransferStateComparisonTest {

        @Test
        @DisplayName("Enum values can be compared using equals")
        fun enumValuesCanBeComparedUsingEquals() {
            assertEquals(TransferState.QUEUED, TransferState.QUEUED)
            assertEquals(TransferState.ACTIVE, TransferState.ACTIVE)
            assertNotEquals(TransferState.QUEUED, TransferState.ACTIVE)
            assertNotEquals(TransferState.DONE, TransferState.FAILED)
        }

        @Test
        @DisplayName("Enum values have the correct ordinal values")
        fun enumValuesHaveCorrectOrdinalValues() {
            val values = TransferState.entries.toTypedArray()
            values.forEachIndexed { index, state ->
                assertEquals(index, state.ordinal)
            }
        }
    }

    @Nested
    @DisplayName("TransferState Error Handling Tests")
    inner class TransferStateErrorHandlingTest {

        @Test
        @DisplayName("valueOf throws IllegalArgumentException for invalid string")
        fun valueOfThrowsExceptionForInvalidString() {
            val exception = assertThrows(IllegalArgumentException::class.java) {
                TransferState.valueOf("INVALID_STATE")
            }
            assertTrue(exception.message?.contains("INVALID_STATE") ?: false)
        }
    }
}