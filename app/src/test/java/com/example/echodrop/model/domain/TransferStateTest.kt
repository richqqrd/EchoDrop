package com.example.echodrop.model.domain

import com.example.echodrop.model.domainLayer.model.TransferState
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
   val states = com.example.echodrop.model.domainLayer.model.TransferState.entries.toTypedArray()

   assertEquals(4, states.size)
   assertTrue(states.contains(com.example.echodrop.model.domainLayer.model.TransferState.QUEUED))
   assertTrue(states.contains(com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE))
   assertTrue(states.contains(com.example.echodrop.model.domainLayer.model.TransferState.DONE))
   assertTrue(states.contains(com.example.echodrop.model.domainLayer.model.TransferState.FAILED))
  }

  @Test
  @DisplayName("valueOf returns correct enum value for string")
  fun valueOfReturnsCorrectEnum() {
   // Test string to enum conversion
   assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.QUEUED, com.example.echodrop.model.domainLayer.model.TransferState.valueOf("QUEUED"))
   assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE, com.example.echodrop.model.domainLayer.model.TransferState.valueOf("ACTIVE"))
   assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.DONE, com.example.echodrop.model.domainLayer.model.TransferState.valueOf("DONE"))
   assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.FAILED, com.example.echodrop.model.domainLayer.model.TransferState.valueOf("FAILED"))
  }

  @Test
  @DisplayName("name returns correct string representation")
  fun nameReturnsCorrectString() {
   // Test enum to string conversion
   assertEquals("QUEUED", com.example.echodrop.model.domainLayer.model.TransferState.QUEUED.name)
   assertEquals("ACTIVE", com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE.name)
   assertEquals("DONE", com.example.echodrop.model.domainLayer.model.TransferState.DONE.name)
   assertEquals("FAILED", com.example.echodrop.model.domainLayer.model.TransferState.FAILED.name)
  }
 }

 @Nested
 @DisplayName("TransferState Comparison Tests")
 inner class TransferStateComparisonTest {

  @Test
  @DisplayName("Enum values can be compared using equals")
  fun enumValuesCanBeComparedUsingEquals() {
   // Same enum values are equal
   assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.QUEUED, com.example.echodrop.model.domainLayer.model.TransferState.QUEUED)
   assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE, com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE)

   // Different enum values are not equal
   assertNotEquals(com.example.echodrop.model.domainLayer.model.TransferState.QUEUED, com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE)
   assertNotEquals(com.example.echodrop.model.domainLayer.model.TransferState.DONE, com.example.echodrop.model.domainLayer.model.TransferState.FAILED)
  }

  @Test
  @DisplayName("Enum values have the correct ordinal values")
  fun enumValuesHaveCorrectOrdinalValues() {
   // Verify the ordinal values match the expected order
   assertEquals(0, com.example.echodrop.model.domainLayer.model.TransferState.QUEUED.ordinal)
   assertEquals(1, com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE.ordinal)
   assertEquals(2, com.example.echodrop.model.domainLayer.model.TransferState.DONE.ordinal)
   assertEquals(3, com.example.echodrop.model.domainLayer.model.TransferState.FAILED.ordinal)
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
    com.example.echodrop.model.domainLayer.model.TransferState.valueOf("INVALID_STATE")
   }

   // Verify the exception message contains useful information
   assertTrue(exception.message?.contains("INVALID_STATE") ?: false)
  }
 }
}