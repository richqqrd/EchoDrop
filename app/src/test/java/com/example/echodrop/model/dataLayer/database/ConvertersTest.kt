package com.example.echodrop.model.dataLayer.database

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

 private lateinit var converters: Converters

 @BeforeEach
 fun setup() {
  converters = Converters()
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
  @DisplayName("Converts TransferState to string")
  fun convertTransferStateToString() {
   // Arrange
   val converters = Converters()

   // Act & Assert
   assertEquals("QUEUED", converters.fromTransferState(TransferState.QUEUED))
   assertEquals("ACTIVE", converters.fromTransferState(TransferState.ACTIVE))
   assertEquals("DONE", converters.fromTransferState(TransferState.DONE))
   assertEquals("FAILED", converters.fromTransferState(TransferState.FAILED))
   assertEquals("PAUSED", converters.fromTransferState(TransferState.PAUSED))
  }

  @Test
  @DisplayName("Converts string to TransferState")
  fun convertStringToTransferState() {
   // Arrange
   val converters = Converters()

   // Act & Assert
   assertEquals(TransferState.QUEUED, converters.toTransferState("QUEUED"))
   assertEquals(TransferState.ACTIVE, converters.toTransferState("ACTIVE"))
   assertEquals(TransferState.DONE, converters.toTransferState("DONE"))
   assertEquals(TransferState.FAILED, converters.toTransferState("FAILED"))
   assertEquals(TransferState.PAUSED, converters.toTransferState("PAUSED"))
  }

  @Test
  @DisplayName("throws IllegalArgumentException for invalid TransferState string")
  fun throwsExceptionForInvalidTransferState() {
   // Arrange
   val converters = Converters()

   // Act & Assert
   val exception = assertThrows(IllegalArgumentException::class.java) {
    converters.toTransferState("INVALID_STATE")
   }

   assertTrue(exception.message?.contains("INVALID_STATE") ?: false)
  }
 }
}