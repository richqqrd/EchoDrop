package com.example.echodrop.model.domain

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `Paket` data class.
 */
class PaketTest {

 @Nested
 @DisplayName("Paket Constructor Tests")
 inner class PaketConstructorTest {

  @Test
  @DisplayName("Creates a Paket with correct properties")
  fun createPaketWithCorrectProperties() {

   val paketId = com.example.echodrop.model.domainLayer.model.PaketId("test-id-123")
   val meta = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Test Paket",
    description = "Test description",
    tags = listOf("test", "example"),
    ttlSeconds = 3600,
    priority = 1
   )
   val fileEntries = listOf(
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "test/file1.txt",
     mime = "text/plain",
     sizeBytes = 1024L,
     orderIdx = 0
    )
   )

   val paket = com.example.echodrop.model.domainLayer.model.Paket(
    id = paketId,
    meta = meta,
    sizeBytes = 1024L,
    sha256 = "abc123hash",
    fileCount = 1,
    createdUtc = 1620000000000L,
    files = fileEntries
   )

   assertEquals(paketId, paket.id)
   assertEquals(meta, paket.meta)
   assertEquals(1024L, paket.sizeBytes)
   assertEquals("abc123hash", paket.sha256)
   assertEquals(1, paket.fileCount)
   assertEquals(1620000000000L, paket.createdUtc)
   assertEquals(fileEntries, paket.files)
  }
 }

 @Nested
 @DisplayName("Paket Equality Tests")
 inner class PaketEqualityTest {

  @Test
  @DisplayName("Two identical Pakets are equal")
  fun twoIdenticalPaketsAreEqual() {
   val paketId = com.example.echodrop.model.domainLayer.model.PaketId("test-id-123")
   val meta = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Test Paket",
    description = "Test description",
    tags = listOf("test", "example"),
    ttlSeconds = 3600,
    priority = 1
   )
   val fileEntries = listOf(
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "test/file1.txt",
     mime = "text/plain",
     sizeBytes = 1024L,
     orderIdx = 0
    )
   )

   val paket1 = com.example.echodrop.model.domainLayer.model.Paket(
    id = paketId,
    meta = meta,
    sizeBytes = 1024L,
    sha256 = "abc123hash",
    fileCount = 1,
    createdUtc = 1620000000000L,
    files = fileEntries
   )

   val paket2 = com.example.echodrop.model.domainLayer.model.Paket(
    id = paketId,
    meta = meta,
    sizeBytes = 1024L,
    sha256 = "abc123hash",
    fileCount = 1,
    createdUtc = 1620000000000L,
    files = fileEntries
   )

   assertEquals(paket1, paket2)
   assertEquals(paket1.hashCode(), paket2.hashCode())
  }

  @Test
  @DisplayName("Pakets with different IDs are not equal")
  fun paketsWithDifferentIdsAreNotEqual() {
   val meta = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Test Paket",
    description = "Test description",
    tags = listOf("test", "example"),
    ttlSeconds = 3600,
    priority = 1
   )
   val fileEntries = listOf(
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "test/file1.txt",
     mime = "text/plain",
     sizeBytes = 1024L,
     orderIdx = 0
    )
   )

   val paket1 = com.example.echodrop.model.domainLayer.model.Paket(
    id = com.example.echodrop.model.domainLayer.model.PaketId("id1"),
    meta = meta,
    sizeBytes = 1024L,
    sha256 = "abc123hash",
    fileCount = 1,
    createdUtc = 1620000000000L,
    files = fileEntries
   )

   val paket2 = com.example.echodrop.model.domainLayer.model.Paket(
    id = com.example.echodrop.model.domainLayer.model.PaketId("id2"),
    meta = meta,
    sizeBytes = 1024L,
    sha256 = "abc123hash",
    fileCount = 1,
    createdUtc = 1620000000000L,
    files = fileEntries
   )

   assertNotEquals(paket1, paket2)
  }
 }

 @Nested
 @DisplayName("Paket Functionality Tests")
 inner class PaketFunctionalityTest {

  @Test
  @DisplayName("FileCount matches the number of files in the Paket")
  fun fileCountMatchesNumberOfFiles() {
   val fileEntries = listOf(
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "file1.txt",
     mime = "text/plain",
     sizeBytes = 100L,
     orderIdx = 0
    ),
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "file2.jpg",
     mime = "image/jpeg",
     sizeBytes = 200L,
     orderIdx = 1
    ),
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "file3.pdf",
     mime = "application/pdf",
     sizeBytes = 300L,
     orderIdx = 2
    )
   )

   val paket = com.example.echodrop.model.domainLayer.model.Paket(
    id = com.example.echodrop.model.domainLayer.model.PaketId("test-id"),
    meta = com.example.echodrop.model.domainLayer.model.PaketMeta(
     "Test",
     null,
     emptyList(),
     3600,
     1
    ),
    sizeBytes = 600L,
    sha256 = "hash",
    fileCount = 3,
    createdUtc = System.currentTimeMillis(),
    files = fileEntries
   )

   assertEquals(fileEntries.size, paket.fileCount)
   assertEquals(fileEntries.size, paket.files.size)
  }

  @Test
  @DisplayName("SizeBytes matches the sum of file sizes")
  fun sizeBytesMatchesSumOfFileSizes() {
   val fileEntries = listOf(
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "file1.txt",
     mime = "text/plain",
     sizeBytes = 100L,
     orderIdx = 0
    ),
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "file2.jpg",
     mime = "image/jpeg",
     sizeBytes = 200L,
     orderIdx = 1
    ),
    com.example.echodrop.model.domainLayer.model.FileEntry(
     path = "file3.pdf",
     mime = "application/pdf",
     sizeBytes = 300L,
     orderIdx = 2
    )
   )

   val expectedTotalSize = fileEntries.sumOf { it.sizeBytes }

   val paket = com.example.echodrop.model.domainLayer.model.Paket(
    id = com.example.echodrop.model.domainLayer.model.PaketId("test-id"),
    meta = com.example.echodrop.model.domainLayer.model.PaketMeta(
     "Test",
     null,
     emptyList(),
     3600,
     1
    ),
    sizeBytes = expectedTotalSize,
    sha256 = "hash",
    fileCount = 3,
    createdUtc = System.currentTimeMillis(),
    files = fileEntries
   )

   assertEquals(expectedTotalSize, paket.sizeBytes)
  }
 }
}