package com.example.echodrop.model.repository

import com.example.echodrop.model.dataLayer.database.daos.PeerDao
import com.example.echodrop.model.dataLayer.database.entities.PeerEntity
import com.example.echodrop.model.dataLayer.repositoryImpl.PeerRepositoryImpl
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
 * Test class for the `PeerRepositoryImpl` implementation.
 */
@DisplayName("PeerRepositoryImpl Tests")
class PeerRepositoryImplTest {

 private lateinit var mockPeerDao: PeerDao

 private lateinit var repository: PeerRepositoryImpl

 private val testPeerId = "peer-123"
 private val testPeerEntities = listOf(
  PeerEntity(
   peerId = testPeerId,
   alias = "Test Device",
   lastSeenUtc = 1620000000000L
  ),
  PeerEntity(
   peerId = "peer-456",
   alias = null,
   lastSeenUtc = 1620000001000L
  )
 )

 @BeforeEach
 fun setup() {
  mockPeerDao = mock(PeerDao::class.java)
  repository = PeerRepositoryImpl(mockPeerDao)
 }

 @Test
 @DisplayName("observeKnownPeers returns mapped domain models from DAO")
 fun observeKnownPeersReturnsMappedDomainModels() = runTest {
  whenever(mockPeerDao.observeAll()).thenReturn(flowOf(testPeerEntities))

  val result = repository.observeKnownPeers().first()

  assertEquals(2, result.size, "Should return two peers")

  assertEquals(com.example.echodrop.model.domainLayer.model.PeerId(testPeerId), result[0].id)
  assertEquals("Test Device", result[0].alias)
  assertEquals(1620000000000L, result[0].lastSeenUtc)

  assertEquals(com.example.echodrop.model.domainLayer.model.PeerId("peer-456"), result[1].id)
  assertNull(result[1].alias)
  assertEquals(1620000001000L, result[1].lastSeenUtc)
 }

 @Test
 @DisplayName("observeKnownPeers returns empty list when no peers found")
 fun observeKnownPeersReturnsEmptyListWhenNoPeersFound() = runTest {
  whenever(mockPeerDao.observeAll()).thenReturn(flowOf(emptyList()))

  val result = repository.observeKnownPeers().first()

  assertTrue(result.isEmpty(), "Should return empty list when no peers found")
 }

 @Test
 @DisplayName("upsertPeer creates peer entity with correct data")
 fun upsertPeerCreatesPeerEntityWithCorrectData() = runTest {
  val testPeer = com.example.echodrop.model.domainLayer.model.Peer(
   id = com.example.echodrop.model.domainLayer.model.PeerId(testPeerId),
   alias = "Test Device",
   lastSeenUtc = 1620000000000L
  )

  val peerEntityCaptor = argumentCaptor<PeerEntity>()

  repository.upsertPeer(testPeer)

  verify(mockPeerDao).upsert(peerEntityCaptor.capture())

  val capturedEntity = peerEntityCaptor.firstValue
  assertEquals(testPeerId, capturedEntity.peerId)
  assertEquals("Test Device", capturedEntity.alias)
  assertEquals(1620000000000L, capturedEntity.lastSeenUtc)
 }

 @Test
 @DisplayName("upsertPeer creates peer entity with null alias")
 fun upsertPeerCreatesPeerEntityWithNullAlias() = runTest {
  val testPeer = com.example.echodrop.model.domainLayer.model.Peer(
   id = com.example.echodrop.model.domainLayer.model.PeerId("peer-456"),
   alias = null,
   lastSeenUtc = 1620000001000L
  )

  val peerEntityCaptor = argumentCaptor<PeerEntity>()

  repository.upsertPeer(testPeer)

  verify(mockPeerDao).upsert(peerEntityCaptor.capture())

  val capturedEntity = peerEntityCaptor.firstValue
  assertEquals("peer-456", capturedEntity.peerId)
  assertNull(capturedEntity.alias)
  assertEquals(1620000001000L, capturedEntity.lastSeenUtc)
 }

 @Test
 @DisplayName("purgeStalePeers calls DAO with correct timestamp")
 fun purgeStalePeersCallsDaoWithCorrectTimestamp() = runTest {
  val cutoffTime = 1620000000000L

  repository.purgeStalePeers(cutoffTime)

  verify(mockPeerDao).purgeStale(cutoffTime)
 }
}