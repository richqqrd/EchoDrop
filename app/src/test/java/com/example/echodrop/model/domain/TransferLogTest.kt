package com.example.echodrop.model.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Test class for the `TransferLog` data class.
 */
class TransferLogTest {

 @Nested
 @DisplayName("TransferLog Construction Tests")
 inner class TransferLogConstructionTest {

  @Test
  @DisplayName("Creates a TransferLog with correct properties")
  fun createTransferLogWithCorrectProperties() {
   // Arrange
   val paketId = PaketId("package-123")
   val peerId = PeerId("peer-456")
   val state = TransferState.ACTIVE
   val progressPct = 75
   val lastUpdateUtc = 1620000000000L

   // Act
   val transferLog = TransferLog(
    paketId = paketId,
    peerId = peerId,
    state = state,
    progressPct = progressPct,
    lastUpdateUtc = lastUpdateUtc
   )

   // Assert
   assertEquals(paketId, transferLog.paketId)
   assertEquals(peerId, transferLog.peerId)
   assertEquals(state, transferLog.state)
   assertEquals(progressPct, transferLog.progressPct)
   assertEquals(lastUpdateUtc, transferLog.lastUpdateUtc)
  }
 }

 @Nested
 @DisplayName("TransferLog Equality Tests")
 inner class TransferLogEqualityTest {

  @Test
  @DisplayName("Two identical TransferLogs are equal")
  fun twoIdenticalTransferLogsAreEqual() {
   // Arrange
   val log1 = TransferLog(
    paketId = PaketId("package-123"),
    peerId = PeerId("peer-456"),
    state = TransferState.ACTIVE,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   val log2 = TransferLog(
    paketId = PaketId("package-123"),
    peerId = PeerId("peer-456"),
    state = TransferState.ACTIVE,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   // Assert
   assertEquals(log1, log2)
   assertEquals(log1.hashCode(), log2.hashCode())
  }

  @Test
  @DisplayName("TransferLogs with different properties are not equal")
  fun transferLogsWithDifferentPropertiesAreNotEqual() {
   // Arrange
   val baseLog = TransferLog(
    paketId = PaketId("package-123"),
    peerId = PeerId("peer-456"),
    state = TransferState.ACTIVE,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   val differentPaketId = baseLog.copy(paketId = PaketId("different-package"))
   val differentPeerId = baseLog.copy(peerId = PeerId("different-peer"))
   val differentState = baseLog.copy(state = TransferState.QUEUED)
   val differentProgress = baseLog.copy(progressPct = 50)
   val differentTimestamp = baseLog.copy(lastUpdateUtc = 1630000000000L)

   // Assert
   assertNotEquals(baseLog, differentPaketId)
   assertNotEquals(baseLog, differentPeerId)
   assertNotEquals(baseLog, differentState)
   assertNotEquals(baseLog, differentProgress)
   assertNotEquals(baseLog, differentTimestamp)
  }
 }

 @Nested
 @DisplayName("TransferLog Progress Tests")
 inner class TransferLogProgressTest {

  @Test
  @DisplayName("Progress percentage must be between 0 and 100")
  fun progressPercentageMustBeValid() {
   // Arrange & Act
   val log = TransferLog(
    paketId = PaketId("package-123"),
    peerId = PeerId("peer-456"),
    state = TransferState.ACTIVE,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   // Assert
   assertTrue(log.progressPct in 0..100)
  }
 }
}