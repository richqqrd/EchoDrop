package com.example.echodrop.model.entities

import com.example.echodrop.domain.model.FileEntry
import com.example.echodrop.domain.model.Paket
import com.example.echodrop.domain.model.PaketId
import com.example.echodrop.model.database.entities.PaketEntity
import com.example.echodrop.model.database.entities.toDomain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `PaketEntity` data class.
 */
class PaketEntityTest {

 @Nested
 @DisplayName("PaketEntity Construction Tests")
 inner class PaketEntityConstructionTest {

  @Test
  @DisplayName("Creates a PaketEntity with correct properties")
  fun createPaketEntityWithCorrectProperties() {
   // Arrange & Act
   val paketId = "paket-123"
   val version = 1
   val title = "Test Package"
   val description = "Description for test package"
   val tags = listOf("test", "sample")
   val sizeBytes = 1024L
   val sha256 = "abc123hash456def"
   val fileCount = 2
   val ttlSeconds = 3600
   val priority = 1
   val hopLimit = 5
   val manifestHash = "manifest-hash-123"
   val createdUtc = 1620000000000L

   val paketEntity = PaketEntity(
    paketId = paketId,
    version = version,
    title = title,
    description = description,
    tags = tags,
    sizeBytes = sizeBytes,
    sha256 = sha256,
    fileCount = fileCount,
    ttlSeconds = ttlSeconds,
    priority = priority,
    hopLimit = hopLimit,
    manifestHash = manifestHash,
    createdUtc = createdUtc
   )

   // Assert
   assertEquals(paketId, paketEntity.paketId)
   assertEquals(version, paketEntity.version)
   assertEquals(title, paketEntity.title)
   assertEquals(description, paketEntity.description)
   assertEquals(tags, paketEntity.tags)
   assertEquals(sizeBytes, paketEntity.sizeBytes)
   assertEquals(sha256, paketEntity.sha256)
   assertEquals(fileCount, paketEntity.fileCount)
   assertEquals(ttlSeconds, paketEntity.ttlSeconds)
   assertEquals(priority, paketEntity.priority)
   assertEquals(hopLimit, paketEntity.hopLimit)
   assertEquals(manifestHash, paketEntity.manifestHash)
   assertEquals(createdUtc, paketEntity.createdUtc)
  }

  @Test
  @DisplayName("Creates a PaketEntity with null description and hopLimit")
  fun createPaketEntityWithNullValues() {
   // Arrange & Act
   val paketEntity = PaketEntity(
    paketId = "paket-123",
    version = 1,
    title = "Test Package",
    description = null,
    tags = listOf("test"),
    sizeBytes = 1024L,
    sha256 = "hash",
    fileCount = 1,
    ttlSeconds = 3600,
    priority = 1,
    hopLimit = null,
    manifestHash = "manifest-hash",
    createdUtc = 1620000000000L
   )

   // Assert
   assertNull(paketEntity.description)
   assertNull(paketEntity.hopLimit)
  }
 }

 @Nested
 @DisplayName("PaketEntity Equality Tests")
 inner class PaketEntityEqualityTest {

  @Test
  @DisplayName("Two identical PaketEntities are equal")
  fun twoIdenticalPaketEntitiesAreEqual() {
   // Arrange
   val entity1 = PaketEntity(
    paketId = "paket-123",
    version = 1,
    title = "Test Package",
    description = "Description",
    tags = listOf("test", "sample"),
    sizeBytes = 1024L,
    sha256 = "hash",
    fileCount = 2,
    ttlSeconds = 3600,
    priority = 1,
    hopLimit = 5,
    manifestHash = "manifest-hash",
    createdUtc = 1620000000000L
   )

   val entity2 = PaketEntity(
    paketId = "paket-123",
    version = 1,
    title = "Test Package",
    description = "Description",
    tags = listOf("test", "sample"),
    sizeBytes = 1024L,
    sha256 = "hash",
    fileCount = 2,
    ttlSeconds = 3600,
    priority = 1,
    hopLimit = 5,
    manifestHash = "manifest-hash",
    createdUtc = 1620000000000L
   )

   // Assert
   assertEquals(entity1, entity2)
   assertEquals(entity1.hashCode(), entity2.hashCode())
  }

  @Test
  @DisplayName("PaketEntities with different properties are not equal")
  fun paketEntitiesWithDifferentPropertiesAreNotEqual() {
   // Arrange
   val baseEntity = PaketEntity(
    paketId = "paket-123",
    version = 1,
    title = "Test Package",
    description = "Description",
    tags = listOf("test"),
    sizeBytes = 1024L,
    sha256 = "hash",
    fileCount = 2,
    ttlSeconds = 3600,
    priority = 1,
    hopLimit = 5,
    manifestHash = "manifest-hash",
    createdUtc = 1620000000000L
   )

   val differentId = baseEntity.copy(paketId = "different-id")
   val differentVersion = baseEntity.copy(version = 2)
   val differentTitle = baseEntity.copy(title = "Different Title")
   val differentDescription = baseEntity.copy(description = "Different description")
   val differentTags = baseEntity.copy(tags = listOf("different", "tags"))
   val differentSize = baseEntity.copy(sizeBytes = 2048L)
   val differentHash = baseEntity.copy(sha256 = "different-hash")
   val differentFileCount = baseEntity.copy(fileCount = 3)
   val differentTtl = baseEntity.copy(ttlSeconds = 7200)
   val differentPriority = baseEntity.copy(priority = 2)
   val differentHopLimit = baseEntity.copy(hopLimit = 10)
   val differentManifestHash = baseEntity.copy(manifestHash = "different-manifest")
   val differentCreatedUtc = baseEntity.copy(createdUtc = 1630000000000L)

   // Assert - test each property difference causes inequality
   assertNotEquals(baseEntity, differentId)
   assertNotEquals(baseEntity, differentVersion)
   assertNotEquals(baseEntity, differentTitle)
   assertNotEquals(baseEntity, differentDescription)
   assertNotEquals(baseEntity, differentTags)
   assertNotEquals(baseEntity, differentSize)
   assertNotEquals(baseEntity, differentHash)
   assertNotEquals(baseEntity, differentFileCount)
   assertNotEquals(baseEntity, differentTtl)
   assertNotEquals(baseEntity, differentPriority)
   assertNotEquals(baseEntity, differentHopLimit)
   assertNotEquals(baseEntity, differentManifestHash)
   assertNotEquals(baseEntity, differentCreatedUtc)
  }
 }

 @Nested
 @DisplayName("PaketEntity Domain Conversion Tests")
 inner class PaketEntityDomainConversionTest {

  @Test
  @DisplayName("toDomain() converts entity to domain model correctly")
  fun toDomainConvertsEntityToDomainModel() {
   // Arrange
   val paketId = "paket-123"
   val title = "Test Package"
   val description = "Description"
   val tags = listOf("test", "sample")
   val sizeBytes = 1024L
   val sha256 = "hash"
   val fileCount = 2
   val ttlSeconds = 3600
   val priority = 1
   val createdUtc = 1620000000000L

   val paketEntity = PaketEntity(
    paketId = paketId,
    version = 1,
    title = title,
    description = description,
    tags = tags,
    sizeBytes = sizeBytes,
    sha256 = sha256,
    fileCount = fileCount,
    ttlSeconds = ttlSeconds,
    priority = priority,
    hopLimit = 5,
    manifestHash = "manifest-hash",
    createdUtc = createdUtc
   )

   val fileEntries = listOf(
    FileEntry("file1.txt", "text/plain", 512L, 0),
    FileEntry("file2.jpg", "image/jpeg", 512L, 1)
   )

   // Act
   val domainModel = paketEntity.toDomain(fileEntries)

   // Assert
   assertTrue(domainModel is Paket)
   assertEquals(PaketId(paketId), domainModel.id)
   assertEquals(title, domainModel.meta.title)
   assertEquals(description, domainModel.meta.description)
   assertEquals(tags, domainModel.meta.tags)
   assertEquals(ttlSeconds, domainModel.meta.ttlSeconds)
   assertEquals(priority, domainModel.meta.priority)
   assertEquals(sizeBytes, domainModel.sizeBytes)
   assertEquals(sha256, domainModel.sha256)
   assertEquals(fileCount, domainModel.fileCount)
   assertEquals(createdUtc, domainModel.createdUtc)
   assertEquals(fileEntries, domainModel.files)
  }

  @Test
  @DisplayName("toDomain() handles null values correctly")
  fun toDomainHandlesNullValuesCorrectly() {
   // Arrange
   val paketEntity = PaketEntity(
    paketId = "paket-123",
    version = 1,
    title = "Test Package",
    description = null,
    tags = emptyList(),
    sizeBytes = 0L,
    sha256 = "",
    fileCount = 0,
    ttlSeconds = 3600,
    priority = 1,
    hopLimit = null,
    manifestHash = "",
    createdUtc = 1620000000000L
   )

   // Act
   val domainModel = paketEntity.toDomain(emptyList())

   // Assert
   assertNull(domainModel.meta.description)
   assertTrue(domainModel.files.isEmpty())
  }
 }
}