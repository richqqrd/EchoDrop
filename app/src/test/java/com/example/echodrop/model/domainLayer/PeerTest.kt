package com.example.echodrop.model.domainLayer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `Peer` data class.
 */
class PeerTest {

 @Nested
 @DisplayName("Peer Construction Tests")
 inner class PeerConstructionTest {

  @Test
  @DisplayName("Creates a Peer with correct properties")
  fun createPeerWithCorrectProperties() {
   // Arrange
   val peerId = com.example.echodrop.model.domainLayer.model.PeerId("test-peer-123")
   val alias = "TestDevice"
   val lastSeenUtc = 1620000000000L

   // Act
   val peer = com.example.echodrop.model.domainLayer.model.Peer(
    id = peerId,
    alias = alias,
    lastSeenUtc = lastSeenUtc
   )

   // Assert
   assertEquals(peerId, peer.id)
   assertEquals(alias, peer.alias)
   assertEquals(lastSeenUtc, peer.lastSeenUtc)
  }

  @Test
  @DisplayName("Creates a Peer with null alias")
  fun createPeerWithNullAlias() {
   // Arrange & Act
   val peer = com.example.echodrop.model.domainLayer.model.Peer(
    id = com.example.echodrop.model.domainLayer.model.PeerId("test-peer-123"),
    alias = null,
    lastSeenUtc = 1620000000000L
   )

   // Assert
   assertNull(peer.alias)
  }
 }

 @Nested
 @DisplayName("Peer Equality Tests")
 inner class PeerEqualityTest {

  @Test
  @DisplayName("Two identical Peers are equal")
  fun twoIdenticalPeersAreEqual() {
   // Arrange
   val id = com.example.echodrop.model.domainLayer.model.PeerId("test-peer-123")
   val alias = "TestDevice"
   val lastSeenUtc = 1620000000000L

   val peer1 = com.example.echodrop.model.domainLayer.model.Peer(id, alias, lastSeenUtc)
   val peer2 = com.example.echodrop.model.domainLayer.model.Peer(id, alias, lastSeenUtc)

   // Assert
   assertEquals(peer1, peer2)
   assertEquals(peer1.hashCode(), peer2.hashCode())
  }

  @Test
  @DisplayName("Peers with different IDs are not equal")
  fun peersWithDifferentIdsAreNotEqual() {
   // Arrange
   val peer1 = com.example.echodrop.model.domainLayer.model.Peer(
    id = com.example.echodrop.model.domainLayer.model.PeerId("peer-1"),
    alias = "Device1",
    lastSeenUtc = 1620000000000L
   )

   val peer2 = com.example.echodrop.model.domainLayer.model.Peer(
    id = com.example.echodrop.model.domainLayer.model.PeerId("peer-2"),
    alias = "Device1",
    lastSeenUtc = 1620000000000L
   )

   // Assert
   assertNotEquals(peer1, peer2)
  }

  @Test
  @DisplayName("Peers with different aliases are not equal")
  fun peersWithDifferentAliasesAreNotEqual() {
   // Arrange
   val id = com.example.echodrop.model.domainLayer.model.PeerId("test-peer-123")
   val lastSeenUtc = 1620000000000L

   val peer1 = com.example.echodrop.model.domainLayer.model.Peer(id, "Device1", lastSeenUtc)
   val peer2 = com.example.echodrop.model.domainLayer.model.Peer(id, "Device2", lastSeenUtc)

   // Assert
   assertNotEquals(peer1, peer2)
  }

  @Test
  @DisplayName("Peers with different lastSeenUtc values are not equal")
  fun peersWithDifferentLastSeenTimesAreNotEqual() {
   // Arrange
   val id = com.example.echodrop.model.domainLayer.model.PeerId("test-peer-123")
   val alias = "TestDevice"

   val peer1 = com.example.echodrop.model.domainLayer.model.Peer(id, alias, 1620000000000L)
   val peer2 = com.example.echodrop.model.domainLayer.model.Peer(id, alias, 1620000001000L)

   // Assert
   assertNotEquals(peer1, peer2)
  }
 }
}