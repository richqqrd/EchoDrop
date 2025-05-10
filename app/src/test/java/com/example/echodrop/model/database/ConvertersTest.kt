package com.example.echodrop.model.database

import com.example.echodrop.model.domainLayer.model.Converters
import com.example.echodrop.model.domainLayer.model.TransferState
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `Converters` utility class.
 */
class ConvertersTest {

 private lateinit var converters: com.example.echodrop.model.domainLayer.model.Converters

 @BeforeEach
 fun setup() {
  converters = com.example.echodrop.model.domainLayer.model.Converters()
 }

 @Nested
 @DisplayName("String List Conversion Tests")
 inner class StringListConversionTests {

  @Test
  @DisplayName("Convert list of strings to JSON and back")
  fun convertListOfStringsToJsonAndBack() {
   // Arrange
   val originalList = listOf("tag1", "tag2", "tag3")

   // Act
   val json = converters.fromStringList(originalList)
   val convertedList = converters.toStringList(json)

   // Assert
   assertEquals(originalList, convertedList)
  }

  @Test
  @DisplayName("Convert empty list to JSON and back")
  fun convertEmptyListToJsonAndBack() {
   // Arrange
   val emptyList = emptyList<String>()

   // Act
   val json = converters.fromStringList(emptyList)
   val convertedList = converters.toStringList(json)

   // Assert
   assertTrue(convertedList.isEmpty())
  }

  @Test
  @DisplayName("Convert null list to empty string")
  fun convertNullListToEmptyString() {
   // Act
   val result = converters.fromStringList(null)

   // Assert
   assertEquals("", result)
  }

  @Test
  @DisplayName("Convert empty string to empty list")
  fun convertEmptyStringToEmptyList() {
   // Act
   val result = converters.toStringList("")

   // Assert
   assertTrue(result.isEmpty())
  }
 }

 @Nested
 @DisplayName("TransferState Conversion Tests")
 inner class TransferStateConversionTests {

  @Test
  @DisplayName("Convert TransferState to string and back")
  fun convertTransferStateToStringAndBack() {
   // Test all enum values
   com.example.echodrop.model.domainLayer.model.TransferState.entries.forEach { state ->
    // Act
    val string = converters.fromTransferState(state)
    val convertedState = converters.toTransferState(string)

    // Assert
    assertEquals(state, convertedState)
   }
  }

  @Test
  @DisplayName("Convert specific TransferState values")
  fun convertSpecificTransferStateValues() {
   // Arrange
   val states = listOf(
    com.example.echodrop.model.domainLayer.model.TransferState.QUEUED,
    com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE,
    com.example.echodrop.model.domainLayer.model.TransferState.DONE,
    com.example.echodrop.model.domainLayer.model.TransferState.FAILED
   )

   // Act & Assert - test each state individually
   states.forEach { state ->
    val string = converters.fromTransferState(state)
    assertEquals(state.name, string)

    val convertedState = converters.toTransferState(string)
    assertEquals(state, convertedState)
   }
  }

  @Test
  @DisplayName("Converting invalid string to TransferState throws exception")
  fun convertInvalidStringThrowsException() {
   // Arrange
   val invalidStateName = "INVALID_STATE"

   // Act & Assert
   val exception = assertThrows(IllegalArgumentException::class.java) {
    converters.toTransferState(invalidStateName)
   }

   // Verify exception contains useful information
   assertTrue(exception.message?.contains(invalidStateName) ?: false)
  }
 }
}