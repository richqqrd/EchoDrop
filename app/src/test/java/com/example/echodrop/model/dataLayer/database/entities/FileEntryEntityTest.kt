package com.example.echodrop.model.dataLayer.database.entities

import com.example.echodrop.model.dataLayer.datasource.persistence.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.toDomain
import com.example.echodrop.model.domainLayer.model.FileEntry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `FileEntryEntity` data class.
 */
class FileEntryEntityTest {

 @Nested
 @DisplayName("FileEntryEntity Construction Tests")
 inner class FileEntryEntityConstructionTest {

  @Test
  @DisplayName("Creates a FileEntryEntity with correct properties")
  fun createFileEntryEntityWithCorrectProperties() {
   // Arrange & Act
   val fileId = "file-123"
   val paketOwnerId = "paket-456"
   val path = "documents/report.pdf"
   val mime = "application/pdf"
   val sizeBytes = 1024L
   val orderIdx = 2

   val fileEntryEntity = FileEntryEntity(
    fileId = fileId,
    paketOwnerId = paketOwnerId,
    path = path,
    mime = mime,
    sizeBytes = sizeBytes,
    orderIdx = orderIdx
   )

   // Assert
   assertEquals(fileId, fileEntryEntity.fileId)
   assertEquals(paketOwnerId, fileEntryEntity.paketOwnerId)
   assertEquals(path, fileEntryEntity.path)
   assertEquals(mime, fileEntryEntity.mime)
   assertEquals(sizeBytes, fileEntryEntity.sizeBytes)
   assertEquals(orderIdx, fileEntryEntity.orderIdx)
  }
 }

 @Nested
 @DisplayName("FileEntryEntity Equality Tests")
 inner class FileEntryEntityEqualityTest {

  @Test
  @DisplayName("Two identical FileEntryEntities are equal")
  fun twoIdenticalFileEntryEntitiesAreEqual() {
   // Arrange
   val entity1 = FileEntryEntity(
    fileId = "file-123",
    paketOwnerId = "paket-456",
    path = "documents/report.pdf",
    mime = "application/pdf",
    sizeBytes = 1024L,
    orderIdx = 2
   )

   val entity2 = FileEntryEntity(
    fileId = "file-123",
    paketOwnerId = "paket-456",
    path = "documents/report.pdf",
    mime = "application/pdf",
    sizeBytes = 1024L,
    orderIdx = 2
   )

   // Assert
   assertEquals(entity1, entity2)
   assertEquals(entity1.hashCode(), entity2.hashCode())
  }

  @Test
  @DisplayName("FileEntryEntities with different properties are not equal")
  fun fileEntryEntitiesWithDifferentPropertiesAreNotEqual() {
   // Arrange
   val baseEntity = FileEntryEntity(
    fileId = "file-123",
    paketOwnerId = "paket-456",
    path = "documents/report.pdf",
    mime = "application/pdf",
    sizeBytes = 1024L,
    orderIdx = 2
   )

   val differentId = baseEntity.copy(fileId = "file-999")
   val differentPaketId = baseEntity.copy(paketOwnerId = "paket-999")
   val differentPath = baseEntity.copy(path = "documents/other.pdf")
   val differentMime = baseEntity.copy(mime = "text/plain")
   val differentSize = baseEntity.copy(sizeBytes = 2048L)
   val differentOrderIdx = baseEntity.copy(orderIdx = 3)

   // Assert
   assertNotEquals(baseEntity, differentId)
   assertNotEquals(baseEntity, differentPaketId)
   assertNotEquals(baseEntity, differentPath)
   assertNotEquals(baseEntity, differentMime)
   assertNotEquals(baseEntity, differentSize)
   assertNotEquals(baseEntity, differentOrderIdx)
  }
 }

 @Nested
 @DisplayName("FileEntryEntity Domain Conversion Tests")
 inner class FileEntryEntityDomainConversionTest {

  @Test
  @DisplayName("toDomain() converts to correct FileEntry object")
  fun toDomainConvertsToCorrectFileEntry() {
   // Arrange
   val entity = FileEntryEntity(
    fileId = "file-123",
    paketOwnerId = "paket-456",
    path = "documents/report.pdf",
    mime = "application/pdf",
    sizeBytes = 1024L,
    orderIdx = 2
   )

   // Act
   val domainModel = entity.toDomain()

   // Assert
   assertTrue(domainModel is FileEntry)
   assertEquals(entity.path, domainModel.path)
   assertEquals(entity.mime, domainModel.mime)
   assertEquals(entity.sizeBytes, domainModel.sizeBytes)
   assertEquals(entity.orderIdx, domainModel.orderIdx)

  }
 }
}