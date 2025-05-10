package com.example.echodrop.model.domain

import com.example.echodrop.model.domainLayer.model.FileEntry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

/**
 * Test class for the `FileEntry` data class.
 */
class FileEntryTest {

 @Test
 @DisplayName("FileEntry should be correctly instantiated with all properties")
 fun testFileEntryCreation() {
  // Given
  val path = "/path/to/file.txt"
  val mime = "text/plain"
  val sizeBytes = 1024L
  val orderIdx = 3

  // When
  val fileEntry =
   com.example.echodrop.model.domainLayer.model.FileEntry(path, mime, sizeBytes, orderIdx)

  // Then
  assertEquals(path, fileEntry.path)
  assertEquals(mime, fileEntry.mime)
  assertEquals(sizeBytes, fileEntry.sizeBytes)
  assertEquals(orderIdx, fileEntry.orderIdx)
 }

 @Test
 @DisplayName("FileEntry instances with same properties should be equal")
 fun testFileEntryEquality() {
  // Given
  val fileEntry1 = com.example.echodrop.model.domainLayer.model.FileEntry(
   "/path/to/file.txt",
   "text/plain",
   1024L,
   3
  )
  val fileEntry2 = com.example.echodrop.model.domainLayer.model.FileEntry(
   "/path/to/file.txt",
   "text/plain",
   1024L,
   3
  )
  val differentFileEntry = com.example.echodrop.model.domainLayer.model.FileEntry(
   "/different/path.txt",
   "text/plain",
   1024L,
   3
  )

  // Then
  assertEquals(fileEntry1, fileEntry2)
  assertNotEquals(fileEntry1, differentFileEntry)
 }

 @Test
 @DisplayName("FileEntry toString() should include all properties")
 fun testFileEntryToString() {
  // Given
  val fileEntry = com.example.echodrop.model.domainLayer.model.FileEntry(
   "/path/to/file.txt",
   "text/plain",
   1024L,
   3
  )

  // When
  val toString = fileEntry.toString()

  // Then
  assertTrue(toString.contains("/path/to/file.txt"))
  assertTrue(toString.contains("text/plain"))
  assertTrue(toString.contains("1024"))
  assertTrue(toString.contains("3"))
 }

 @Test
 @DisplayName("FileEntry should handle empty values correctly")
 fun testFileEntryWithEmptyValues() {
  // Given
  val fileEntry = com.example.echodrop.model.domainLayer.model.FileEntry("", "", 0L, 0)

  // Then
  assertEquals("", fileEntry.path)
  assertEquals("", fileEntry.mime)
  assertEquals(0L, fileEntry.sizeBytes)
  assertEquals(0, fileEntry.orderIdx)
 }

 @Test
 @DisplayName("FileEntry copy() should work correctly")
 fun testFileEntryCopy() {
  // Given
  val original = com.example.echodrop.model.domainLayer.model.FileEntry(
   "/path/to/file.txt",
   "text/plain",
   1024L,
   3
  )

  // When
  val copied = original.copy(mime = "application/pdf")

  // Then
  assertEquals("/path/to/file.txt", copied.path)
  assertEquals("application/pdf", copied.mime)
  assertEquals(1024L, copied.sizeBytes)
  assertEquals(3, copied.orderIdx)
 }
}