package com.example.echodrop.model.domain

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferLog
import com.example.echodrop.model.domainLayer.model.TransferState
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
   val paketId = com.example.echodrop.model.domainLayer.model.PaketId("package-123")
   val peerId = com.example.echodrop.model.domainLayer.model.PeerId("peer-456")
   val state = com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE
   val progressPct = 75
   val lastUpdateUtc = 1620000000000L

   // Act
   val transferLog = com.example.echodrop.model.domainLayer.model.TransferLog(
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
   val log1 = com.example.echodrop.model.domainLayer.model.TransferLog(
    paketId = com.example.echodrop.model.domainLayer.model.PaketId("package-123"),
    peerId = com.example.echodrop.model.domainLayer.model.PeerId("peer-456"),
    state = com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   val log2 = com.example.echodrop.model.domainLayer.model.TransferLog(
    paketId = com.example.echodrop.model.domainLayer.model.PaketId("package-123"),
    peerId = com.example.echodrop.model.domainLayer.model.PeerId("peer-456"),
    state = com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE,
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
   val baseLog = com.example.echodrop.model.domainLayer.model.TransferLog(
    paketId = com.example.echodrop.model.domainLayer.model.PaketId("package-123"),
    peerId = com.example.echodrop.model.domainLayer.model.PeerId("peer-456"),
    state = com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   val differentPaketId = baseLog.copy(paketId = com.example.echodrop.model.domainLayer.model.PaketId(
    "different-package"
   )
   )
   val differentPeerId = baseLog.copy(peerId = com.example.echodrop.model.domainLayer.model.PeerId("different-peer"))
   val differentState = baseLog.copy(state = com.example.echodrop.model.domainLayer.model.TransferState.QUEUED)
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
   val log = com.example.echodrop.model.domainLayer.model.TransferLog(
    paketId = com.example.echodrop.model.domainLayer.model.PaketId("package-123"),
    peerId = com.example.echodrop.model.domainLayer.model.PeerId("peer-456"),
    state = com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE,
    progressPct = 75,
    lastUpdateUtc = 1620000000000L
   )

   // Assert
   assertTrue(log.progressPct in 0..100)
  }
 }
}