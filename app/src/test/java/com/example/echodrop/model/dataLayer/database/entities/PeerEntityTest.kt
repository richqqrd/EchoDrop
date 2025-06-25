package com.example.echodrop.model.dataLayer.database.entities

import com.example.echodrop.model.dataLayer.database.entities.PeerEntity
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `PeerEntity` data class.
 */
class PeerEntityTest {

 @Nested
 @DisplayName("PeerEntity Construction Tests")
 inner class PeerEntityConstructionTest {

  @Test
  @DisplayName("Creates a PeerEntity with correct properties")
  fun createPeerEntityWithCorrectProperties() {
   // Arrange & Act
   val peerId = "peer-123"
   val alias = "Test Device"
   val lastSeenUtc = 1620000000000L

   val peerEntity = PeerEntity(
    peerId = peerId,
    alias = alias,
    lastSeenUtc = lastSeenUtc
   )

   // Assert
   assertEquals(peerId, peerEntity.peerId)
   assertEquals(alias, peerEntity.alias)
   assertEquals(lastSeenUtc, peerEntity.lastSeenUtc)
  }

  @Test
  @DisplayName("Creates a PeerEntity with null alias")
  fun createPeerEntityWithNullAlias() {
   // Arrange & Act
   val peerEntity = PeerEntity(
    peerId = "peer-123",
    alias = null,
    lastSeenUtc = 1620000000000L
   )

   // Assert
   assertNull(peerEntity.alias)
  }
 }

 @Nested
 @DisplayName("PeerEntity Equality Tests")
 inner class PeerEntityEqualityTest {

  @Test
  @DisplayName("Two identical PeerEntities are equal")
  fun twoIdenticalPeerEntitiesAreEqual() {
   // Arrange
   val entity1 = PeerEntity(
    peerId = "peer-123",
    alias = "Test Device",
    lastSeenUtc = 1620000000000L
   )

   val entity2 = PeerEntity(
    peerId = "peer-123",
    alias = "Test Device",
    lastSeenUtc = 1620000000000L
   )

   // Assert
   assertEquals(entity1, entity2)
   assertEquals(entity1.hashCode(), entity2.hashCode())
  }

  @Test
  @DisplayName("PeerEntities with different properties are not equal")
  fun peerEntitiesWithDifferentPropertiesAreNotEqual() {
   // Arrange
   val baseEntity = PeerEntity(
    peerId = "peer-123",
    alias = "Test Device",
    lastSeenUtc = 1620000000000L
   )

   val differentId = baseEntity.copy(peerId = "peer-456")
   val differentAlias = baseEntity.copy(alias = "Another Device")
   val differentLastSeen = baseEntity.copy(lastSeenUtc = 1630000000000L)

   // Assert
   assertNotEquals(baseEntity, differentId)
   assertNotEquals(baseEntity, differentAlias)
   assertNotEquals(baseEntity, differentLastSeen)
  }

  @Test
  @DisplayName("PeerEntity with null alias is different from one with non-null alias")
  fun peerEntityWithNullAliasIsDifferentFromNonNull() {
   // Arrange
   val entityWithAlias = PeerEntity(
    peerId = "peer-123",
    alias = "Test Device",
    lastSeenUtc = 1620000000000L
   )

   val entityWithoutAlias = PeerEntity(
    peerId = "peer-123",
    alias = null,
    lastSeenUtc = 1620000000000L
   )

   // Assert
   assertNotEquals(entityWithAlias, entityWithoutAlias)
  }
 }

}