package com.example.echodrop.model.dataLayer.database.entities

import com.example.echodrop.model.dataLayer.database.entities.ChunkEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `ChunkEntity` data class.
 */
class ChunkEntityTest {

 @Nested
 @DisplayName("ChunkEntity Construction Tests")
 inner class ChunkEntityConstructionTest {

  @Test
  @DisplayName("Creates a ChunkEntity with correct properties")
  fun createChunkEntityWithCorrectProperties() {
   // Arrange & Act
   val chunkId = "chunk-123"
   val fileId = "file-456"
   val offset = 1024L
   val size = 512
   val sha256 = "abc123hash456def"
   val completed = true

   val chunkEntity = ChunkEntity(
    chunkId = chunkId,
    fileId = fileId,
    offset = offset,
    size = size,
    sha256 = sha256,
    completed = completed
   )

   // Assert
   assertEquals(chunkId, chunkEntity.chunkId)
   assertEquals(fileId, chunkEntity.fileId)
   assertEquals(offset, chunkEntity.offset)
   assertEquals(size, chunkEntity.size)
   assertEquals(sha256, chunkEntity.sha256)
   assertEquals(completed, chunkEntity.completed)
  }
 }

 @Nested
 @DisplayName("ChunkEntity Equality Tests")
 inner class ChunkEntityEqualityTest {

  @Test
  @DisplayName("Two identical ChunkEntities are equal")
  fun twoIdenticalChunkEntitiesAreEqual() {
   // Arrange
   val chunk1 = ChunkEntity(
    chunkId = "chunk-123",
    fileId = "file-456",
    offset = 1024L,
    size = 512,
    sha256 = "abc123hash456def",
    completed = true
   )

   val chunk2 = ChunkEntity(
    chunkId = "chunk-123",
    fileId = "file-456",
    offset = 1024L,
    size = 512,
    sha256 = "abc123hash456def",
    completed = true
   )

   // Assert
   assertEquals(chunk1, chunk2)
   assertEquals(chunk1.hashCode(), chunk2.hashCode())
  }

  @Test
  @DisplayName("ChunkEntities with different properties are not equal")
  fun chunkEntitiesWithDifferentPropertiesAreNotEqual() {
   // Arrange
   val baseChunk = ChunkEntity(
    chunkId = "chunk-123",
    fileId = "file-456",
    offset = 1024L,
    size = 512,
    sha256 = "abc123hash456def",
    completed = true
   )

   val differentId = baseChunk.copy(chunkId = "chunk-999")
   val differentFileId = baseChunk.copy(fileId = "file-999")
   val differentOffset = baseChunk.copy(offset = 2048L)
   val differentSize = baseChunk.copy(size = 1024)
   val differentHash = baseChunk.copy(sha256 = "different-hash")
   val differentCompleted = baseChunk.copy(completed = false)

   // Assert
   assertNotEquals(baseChunk, differentId)
   assertNotEquals(baseChunk, differentFileId)
   assertNotEquals(baseChunk, differentOffset)
   assertNotEquals(baseChunk, differentSize)
   assertNotEquals(baseChunk, differentHash)
   assertNotEquals(baseChunk, differentCompleted)
  }
 }

 @Nested
 @DisplayName("ChunkEntity Property Tests")
 inner class ChunkEntityPropertyTest {

  @Test
  @DisplayName("Size property can be zero")
  fun sizePropertyCanBeZero() {
   // Arrange & Act
   val chunk = ChunkEntity(
    chunkId = "empty-chunk",
    fileId = "file-456",
    offset = 0L,
    size = 0,
    sha256 = "empty-hash",
    completed = true
   )

   // Assert
   assertEquals(0, chunk.size)
  }

  @Test
  @DisplayName("Completed property reflects chunk processing status")
  fun completedPropertyReflectsChunkStatus() {
   // Arrange & Act
   val processedChunk = ChunkEntity(
    chunkId = "chunk-123",
    fileId = "file-456",
    offset = 1024L,
    size = 512,
    sha256 = "hash",
    completed = true
   )

   val unprocessedChunk = ChunkEntity(
    chunkId = "chunk-123",
    fileId = "file-456",
    offset = 1024L,
    size = 512,
    sha256 = "hash",
    completed = false
   )

   // Assert
   assertTrue(processedChunk.completed)
   assertFalse(unprocessedChunk.completed)
  }
 }
}