package com.example.echodrop.model.domainLayer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `PeerId` value class.
 */
class PeerIdTest {

 @Nested
 @DisplayName("PeerId Construction Tests")
 inner class PeerIdConstructionTest {

  @Test
  @DisplayName("Creates a PeerId with the correct value")
  fun createPeerIdWithCorrectValue() {
   // Arrange & Act
   val id = "test-peer-123"
   val peerId = com.example.echodrop.model.domainLayer.model.PeerId(id)

   // Assert
   assertEquals(id, peerId.value)
  }
 }

 @Nested
 @DisplayName("PeerId Equality Tests")
 inner class PeerIdEqualityTest {

  @Test
  @DisplayName("Two PeerIds with the same value are equal")
  fun twoIdenticalPeerIdsAreEqual() {
   // Arrange & Act
   val id1 = com.example.echodrop.model.domainLayer.model.PeerId("test-peer-123")
   val id2 = com.example.echodrop.model.domainLayer.model.PeerId("test-peer-123")

   // Assert
   assertEquals(id1, id2)
   assertEquals(id1.hashCode(), id2.hashCode())
  }

  @Test
  @DisplayName("Two PeerIds with different values are not equal")
  fun twoDifferentPeerIdsAreNotEqual() {
   // Arrange & Act
   val id1 = com.example.echodrop.model.domainLayer.model.PeerId("peer-1")
   val id2 = com.example.echodrop.model.domainLayer.model.PeerId("peer-2")

   // Assert
   assertNotEquals(id1, id2)
   assertNotEquals(id1.hashCode(), id2.hashCode())
  }
 }

 @Nested
 @DisplayName("PeerId String Representation Tests")
 inner class PeerIdStringRepresentationTest {

  @Test
  @DisplayName("toString returns PeerId(value=x) format by default")
  fun toStringReturnsStandardFormat() {
   // Arrange
   val idValue = "test-peer-123"
   val peerId = com.example.echodrop.model.domainLayer.model.PeerId(idValue)

   // Act & Assert
   assertEquals("PeerId(value=$idValue)", peerId.toString())
  }
 }
}