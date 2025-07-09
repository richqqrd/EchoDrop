package com.example.echodrop.model.dataLayer.database.entities

import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.TransferLogEntity
import com.example.echodrop.model.domainLayer.model.TransferDirection
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Tests if entities with the same combination of `paketId` and `peerId` are considered equal.
 */
class TransferLogEntityTest {

 @Nested
 @DisplayName("TransferLogEntity Construction Tests")
 inner class TransferLogEntityConstructionTest {

  @Test
  @DisplayName("Creates a TransferLogEntity with correct properties")
  fun createTransferLogEntityWithCorrectProperties() {
   // Arrange & Act
   val paketId = "package-123"
   val peerId = "peer-456"
   val state = TransferState.ACTIVE
   val progressPct = 75
   val direction = TransferDirection.OUTGOING
   val lastUpdateUtc = 1620000000000L

   val transferLogEntity = TransferLogEntity(
    paketId = paketId,
    peerId = peerId,
    state = state,
    direction = direction,
    progressPct = progressPct,
    lastUpdateUtc = lastUpdateUtc
   )

   // Assert
   assertEquals(paketId, transferLogEntity.paketId)
   assertEquals(peerId, transferLogEntity.peerId)
   assertEquals(state, transferLogEntity.state)
   assertEquals(direction, transferLogEntity.direction)
   assertEquals(progressPct, transferLogEntity.progressPct)
   assertEquals(lastUpdateUtc, transferLogEntity.lastUpdateUtc)
  }
 }

 @Nested
 @DisplayName("TransferLogEntity Equality Tests")
 inner class TransferLogEntityEqualityTest {

  @Test
  @DisplayName("Two identical TransferLogEntities are equal")
  fun twoIdenticalTransferLogEntitiesAreEqual() {
   // Arrange
   val entity1 = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.ACTIVE,
    direction = TransferDirection.OUTGOING,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   val entity2 = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.ACTIVE,
    direction = TransferDirection.OUTGOING,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   // Assert
   assertEquals(entity1, entity2)
   assertEquals(entity1.hashCode(), entity2.hashCode())
  }

  @Test
  @DisplayName("TransferLogEntities with different properties are not equal")
  fun transferLogEntitiesWithDifferentPropertiesAreNotEqual() {
   // Arrange
   val baseEntity = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.ACTIVE,
    direction = TransferDirection.OUTGOING,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   val differentPaketId = baseEntity.copy(paketId = "package-456")
   val differentPeerId = baseEntity.copy(peerId = "peer-789")
   val differentState = baseEntity.copy(state = TransferState.QUEUED)
   val differentDirection = baseEntity.copy(direction = TransferDirection.INCOMING)
   val differentProgress = baseEntity.copy(progressPct = 50)
   val differentTimestamp = baseEntity.copy(lastUpdateUtc = 1630000000000L)

   // Assert
   assertNotEquals(baseEntity, differentPaketId)
   assertNotEquals(baseEntity, differentPeerId)
   assertNotEquals(baseEntity, differentState)
   assertNotEquals(baseEntity, differentDirection)
   assertNotEquals(baseEntity, differentProgress)
   assertNotEquals(baseEntity, differentTimestamp)
  }
 }

 @Nested
 @DisplayName("TransferLogEntity State Tests")
 inner class TransferLogEntityStateTest {

  @Test
  @DisplayName("Can create TransferLogEntity with different transfer states")
  fun createTransferLogEntityWithDifferentStates() {
   // Arrange & Act - Create entities with different states
   val queuedEntity = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.QUEUED,
    direction = TransferDirection.OUTGOING,
    progressPct = 0,
    lastUpdateUtc = 1620000000000L
   )

   val activeEntity = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.ACTIVE,
    direction = TransferDirection.OUTGOING,
    progressPct = 50,
    lastUpdateUtc = 1620000000001L
   )

   val doneEntity = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.DONE,
    direction = TransferDirection.OUTGOING,
    progressPct = 100,
    lastUpdateUtc = 1620000000002L
   )

   val failedEntity = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.FAILED,
    direction = TransferDirection.OUTGOING,
    progressPct = 75,
    lastUpdateUtc = 1620000000003L
   )

   // Assert
   assertEquals(TransferState.QUEUED, queuedEntity.state)
   assertEquals(TransferState.ACTIVE, activeEntity.state)
   assertEquals(TransferState.DONE, doneEntity.state)
   assertEquals(TransferState.FAILED, failedEntity.state)
  }
 }

 @Nested
 @DisplayName("TransferLogEntity Composite Key Tests")
 inner class TransferLogEntityCompositeKeyTest {

  @Test
  @DisplayName("Entities with same ID combination are equal")
  fun entitiesWithSameIdCombinationAreNotEqual() {
   // Arrange
   val entity1 = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.ACTIVE,
    direction = TransferDirection.OUTGOING,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   // Different state, progress and timestamp but same IDs
   val entity2 = TransferLogEntity(
    paketId = "package-123",
    peerId = "peer-456",
    state = TransferState.DONE,
    direction = TransferDirection.OUTGOING,
    progressPct = 100,
    lastUpdateUtc = 1630000000000L
   )


   assertNotEquals(entity1, entity2)
  }
 }
}