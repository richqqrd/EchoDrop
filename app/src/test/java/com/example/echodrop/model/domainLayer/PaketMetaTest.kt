package com.example.echodrop.model.domainLayer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `PaketMeta` data class.
 */
class PaketMetaTest {

 @Nested
 @DisplayName("PaketMeta Construction Tests")
 inner class PaketMetaConstructionTest {

  @Test
  @DisplayName("Creates a PaketMeta with correct properties")
  fun createPaketMetaWithCorrectProperties() {
   // Arrange & Act
   val title = "Sample Package"
   val description = "This is a test package"
   val tags = listOf("test", "sample", "demo")
   val ttlSeconds = 3600
   val priority = 2

   val paketMeta = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = title,
    description = description,
    tags = tags,
    ttlSeconds = ttlSeconds,
    priority = priority
   )

   // Assert
   assertEquals(title, paketMeta.title)
   assertEquals(description, paketMeta.description)
   assertEquals(tags, paketMeta.tags)
   assertEquals(ttlSeconds, paketMeta.ttlSeconds)
   assertEquals(priority, paketMeta.priority)
  }

  @Test
  @DisplayName("Creates a PaketMeta with null description")
  fun createPaketMetaWithNullDescription() {
   // Arrange & Act
   val paketMeta = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Sample Package",
    description = null,
    tags = listOf("test"),
    ttlSeconds = 3600,
    priority = 1
   )

   // Assert
   assertNull(paketMeta.description)
  }
 }

 @Nested
 @DisplayName("PaketMeta Equality Tests")
 inner class PaketMetaEqualityTest {

  @Test
  @DisplayName("Two identical PaketMeta objects are equal")
  fun twoIdenticalPaketMetasAreEqual() {
   // Arrange
   val meta1 = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Test Package",
    description = "Description",
    tags = listOf("tag1", "tag2"),
    ttlSeconds = 7200,
    priority = 3
   )

   val meta2 = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Test Package",
    description = "Description",
    tags = listOf("tag1", "tag2"),
    ttlSeconds = 7200,
    priority = 3
   )

   // Assert
   assertEquals(meta1, meta2)
   assertEquals(meta1.hashCode(), meta2.hashCode())
  }

  @Test
  @DisplayName("Two PaketMeta objects with different properties are not equal")
  fun differentPaketMetasAreNotEqual() {
   // Arrange
   val meta1 = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Test Package 1",
    description = "Description 1",
    tags = listOf("tag1", "tag2"),
    ttlSeconds = 7200,
    priority = 3
   )

   val meta2 = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Test Package 2",
    description = "Description 2",
    tags = listOf("tag1", "tag2"),
    ttlSeconds = 7200,
    priority = 3
   )

   // Assert
   assertNotEquals(meta1, meta2)
  }
 }

 @Nested
 @DisplayName("PaketMeta Tags Tests")
 inner class PaketMetaTagsTest {

  @Test
  @DisplayName("PaketMeta can have empty tags list")
  fun paketMetaCanHaveEmptyTagsList() {
   // Arrange & Act
   val paketMeta = com.example.echodrop.model.domainLayer.model.PaketMeta(
    title = "Package with no tags",
    description = "A package with empty tags list",
    tags = emptyList(),
    ttlSeconds = 3600,
    priority = 1
   )

   // Assert
   assertTrue(paketMeta.tags.isEmpty())
  }
 }
}