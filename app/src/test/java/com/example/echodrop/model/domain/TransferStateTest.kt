package com.example.echodrop.model.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `TransferState` enum class.
 */
class TransferStateTest {

 @Nested
 @DisplayName("TransferState Basic Tests")
 inner class TransferStateBasicTest {

  @Test
  @DisplayName("Enum contains all expected states")
  fun enumContainsAllExpectedStates() {
   // Assert that all required states exist
   val states = TransferState.entries.toTypedArray()

   assertEquals(4, states.size)
   assertTrue(states.contains(TransferState.QUEUED))
   assertTrue(states.contains(TransferState.ACTIVE))
   assertTrue(states.contains(TransferState.DONE))
   assertTrue(states.contains(TransferState.FAILED))
  }

  @Test
  @DisplayName("valueOf returns correct enum value for string")
  fun valueOfReturnsCorrectEnum() {
   // Test string to enum conversion
   assertEquals(TransferState.QUEUED, TransferState.valueOf("QUEUED"))
   assertEquals(TransferState.ACTIVE, TransferState.valueOf("ACTIVE"))
   assertEquals(TransferState.DONE, TransferState.valueOf("DONE"))
   assertEquals(TransferState.FAILED, TransferState.valueOf("FAILED"))
  }

  @Test
  @DisplayName("name returns correct string representation")
  fun nameReturnsCorrectString() {
   // Test enum to string conversion
   assertEquals("QUEUED", TransferState.QUEUED.name)
   assertEquals("ACTIVE", TransferState.ACTIVE.name)
   assertEquals("DONE", TransferState.DONE.name)
   assertEquals("FAILED", TransferState.FAILED.name)
  }
 }

 @Nested
 @DisplayName("TransferState Comparison Tests")
 inner class TransferStateComparisonTest {

  @Test
  @DisplayName("Enum values can be compared using equals")
  fun enumValuesCanBeComparedUsingEquals() {
   // Same enum values are equal
   assertEquals(TransferState.QUEUED, TransferState.QUEUED)
   assertEquals(TransferState.ACTIVE, TransferState.ACTIVE)

   // Different enum values are not equal
   assertNotEquals(TransferState.QUEUED, TransferState.ACTIVE)
   assertNotEquals(TransferState.DONE, TransferState.FAILED)
  }

  @Test
  @DisplayName("Enum values have the correct ordinal values")
  fun enumValuesHaveCorrectOrdinalValues() {
   // Verify the ordinal values match the expected order
   assertEquals(0, TransferState.QUEUED.ordinal)
   assertEquals(1, TransferState.ACTIVE.ordinal)
   assertEquals(2, TransferState.DONE.ordinal)
   assertEquals(3, TransferState.FAILED.ordinal)
  }
 }

 @Nested
 @DisplayName("TransferState Error Handling Tests")
 inner class TransferStateErrorHandlingTest {

  @Test
  @DisplayName("valueOf throws IllegalArgumentException for invalid string")
  fun valueOfThrowsExceptionForInvalidString() {
   // Assert that an exception is thrown for an invalid state name
   val exception = assertThrows(IllegalArgumentException::class.java) {
    TransferState.valueOf("INVALID_STATE")
   }

   // Verify the exception message contains useful information
   assertTrue(exception.message?.contains("INVALID_STATE") ?: false)
  }
 }
}