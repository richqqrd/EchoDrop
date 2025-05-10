package com.example.echodrop.model.repository

import com.example.echodrop.model.dataLayer.database.daos.TransferDao
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.dataLayer.database.entities.TransferLogEntity
import com.example.echodrop.model.dataLayer.repositoryImpl.TransferRepositoryImpl
import com.example.echodrop.model.domainLayer.transport.TransportManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Test class for the `TransferRepositoryImpl` implementation.
 */
@DisplayName("TransferRepositoryImpl Tests")
class TransferRepositoryImplTest {

 private lateinit var mockTransferDao: TransferDao
 private lateinit var repository: TransferRepositoryImpl
 private lateinit var mockTransportManager: TransportManager

 private val testPaketId = "paket-123"
 private val testPeerId = "peer-456"
 private val testTransferLogEntities = listOf(
  TransferLogEntity(
   paketId = testPaketId,
   peerId = testPeerId,
   state = TransferState.ACTIVE,
   progressPct = 50,
   lastUpdateUtc = 1620000000000L
  ),
  TransferLogEntity(
   paketId = "paket-789",
   peerId = "peer-012",
   state = TransferState.QUEUED,
   progressPct = 0,
   lastUpdateUtc = 1620000001000L
  )
 )

 @BeforeEach
 fun setup() {
  mockTransferDao = mock(TransferDao::class.java)
  mockTransportManager = mock(TransportManager::class.java)
  repository = TransferRepositoryImpl(mockTransferDao, mockTransportManager)

 }

 @Test
 @DisplayName("observeTransfers returns mapped domain models from DAO")
 fun observeTransfersReturnsMappedDomainModels() = runTest {
  whenever(mockTransferDao.observeAll()).thenReturn(flowOf(testTransferLogEntities))

  val result = repository.observeTransfers().first()

  assertEquals(2, result.size)

  assertEquals(com.example.echodrop.model.domainLayer.model.PaketId(testPaketId), result[0].paketId)
  assertEquals(com.example.echodrop.model.domainLayer.model.PeerId(testPeerId), result[0].peerId)
  assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE, result[0].state)
  assertEquals(50, result[0].progressPct)
  assertEquals(1620000000000L, result[0].lastUpdateUtc)

  assertEquals(com.example.echodrop.model.domainLayer.model.PaketId("paket-789"), result[1].paketId)
  assertEquals(com.example.echodrop.model.domainLayer.model.PeerId("peer-012"), result[1].peerId)
  assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.QUEUED, result[1].state)
  assertEquals(0, result[1].progressPct)
  assertEquals(1620000001000L, result[1].lastUpdateUtc)
 }

 @Test
 @DisplayName("pause updates transfer state to QUEUED")
 fun pauseUpdatesTransferStateToQueued() = runTest {
  val testEntity = TransferLogEntity(
   paketId = testPaketId,
   peerId = testPeerId,
   state = TransferState.ACTIVE,
   progressPct = 50,
   lastUpdateUtc = 1620000000000L
  )

  whenever(mockTransferDao.findById(testPaketId, testPeerId)).thenReturn(testEntity)

  val entityCaptor = argumentCaptor<TransferLogEntity>()

  repository.pause(
   com.example.echodrop.model.domainLayer.model.PaketId(testPaketId),
   com.example.echodrop.model.domainLayer.model.PeerId(testPeerId)
  )

  verify(mockTransferDao).upsert(entityCaptor.capture())

  val updatedEntity = entityCaptor.firstValue
  assertEquals(testPaketId, updatedEntity.paketId)
  assertEquals(testPeerId, updatedEntity.peerId)
  assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.QUEUED, updatedEntity.state)
  assertEquals(50, updatedEntity.progressPct)
  assertTrue(updatedEntity.lastUpdateUtc >= 1620000000000L)
 }

 @Test
 @DisplayName("pause does nothing when transfer not found")
 fun pauseDoesNothingWhenTransferNotFound() = runTest {
  whenever(mockTransferDao.findById(testPaketId, testPeerId)).thenReturn(null)

  repository.pause(
   com.example.echodrop.model.domainLayer.model.PaketId(testPaketId),
   com.example.echodrop.model.domainLayer.model.PeerId(testPeerId)
  )

  // Verify findById was called, then verify no other methods were called
  verify(mockTransferDao).findById(testPaketId, testPeerId)
  verifyNoMoreInteractions(mockTransferDao)
 }

 @Test
 @DisplayName("resume updates transfer state to ACTIVE")
 fun resumeUpdatesTransferStateToActive() = runTest {
  val testEntity = TransferLogEntity(
   paketId = testPaketId,
   peerId = testPeerId,
   state = TransferState.QUEUED,
   progressPct = 50,
   lastUpdateUtc = 1620000000000L
  )

  whenever(mockTransferDao.findById(testPaketId, testPeerId)).thenReturn(testEntity)

  val entityCaptor = argumentCaptor<TransferLogEntity>()

  repository.resume(
   com.example.echodrop.model.domainLayer.model.PaketId(testPaketId),
   com.example.echodrop.model.domainLayer.model.PeerId(testPeerId)
  )

  verify(mockTransferDao).upsert(entityCaptor.capture())

  val updatedEntity = entityCaptor.firstValue
  assertEquals(testPaketId, updatedEntity.paketId)
  assertEquals(testPeerId, updatedEntity.peerId)
  assertEquals(com.example.echodrop.model.domainLayer.model.TransferState.ACTIVE, updatedEntity.state)
  assertEquals(50, updatedEntity.progressPct)
  assertTrue(updatedEntity.lastUpdateUtc >= 1620000000000L)
 }

 @Test
 @DisplayName("resume does nothing when transfer not found")
 fun resumeDoesNothingWhenTransferNotFound() = runTest {
  whenever(mockTransferDao.findById(testPaketId, testPeerId)).thenReturn(null)

  repository.resume(
   com.example.echodrop.model.domainLayer.model.PaketId(testPaketId),
   com.example.echodrop.model.domainLayer.model.PeerId(testPeerId)
  )

  // Verify findById was called, then verify no other methods were called
  verify(mockTransferDao).findById(testPaketId, testPeerId)
  verifyNoMoreInteractions(mockTransferDao)
 }

 @Test
 @DisplayName("cancel calls DAO delete with correct IDs")
 fun cancelCallsDaoDeleteWithCorrectIds() = runTest {
  repository.cancel(
   com.example.echodrop.model.domainLayer.model.PaketId(testPaketId),
   com.example.echodrop.model.domainLayer.model.PeerId(testPeerId)
  )

  verify(mockTransferDao).delete(testPaketId, testPeerId)
 }
}