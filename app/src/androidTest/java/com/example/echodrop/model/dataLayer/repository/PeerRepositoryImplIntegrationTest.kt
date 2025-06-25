package com.example.echodrop.model.dataLayer.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.database.EchoDatabase
import com.example.echodrop.model.dataLayer.repositoryImpl.PeerRepositoryImpl
import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.model.PeerId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for PeerRepositoryImpl
 */
@RunWith(AndroidJUnit4::class)
class PeerRepositoryIntegrationTest {
    private lateinit var db: EchoDatabase
    private lateinit var repository: PeerRepositoryImpl

    private val testPeer = Peer(
        id = PeerId("test-peer-123"),
        alias = "Test Device",
        lastSeenUtc = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            EchoDatabase::class.java
        ).build()
        repository = PeerRepositoryImpl(db.peerDao())
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insertAndObservePeer() = runTest {
        repository.upsertPeer(testPeer)
        
        val peers = repository.observeKnownPeers().first()
        
        assertEquals(1, peers.size)
        assertEquals(testPeer.id, peers[0].id)
        assertEquals(testPeer.alias, peers[0].alias)
        assertEquals(testPeer.lastSeenUtc, peers[0].lastSeenUtc)
    }

    @Test
    fun updateExistingPeer() = runTest {
        repository.upsertPeer(testPeer)
        
        val updatedPeer = testPeer.copy(
            alias = "Updated Device",
            lastSeenUtc = System.currentTimeMillis()
        )
        repository.upsertPeer(updatedPeer)
        
        val peers = repository.observeKnownPeers().first()
        assertEquals(1, peers.size)
        assertEquals(updatedPeer.alias, peers[0].alias)
        assertTrue(peers[0].lastSeenUtc >= testPeer.lastSeenUtc)
    }

    @Test
    fun insertMultiplePeers() = runTest {
        val peers = listOf(
            testPeer,
            Peer(PeerId("test-peer-456"), "Second Device", System.currentTimeMillis()),
            Peer(PeerId("test-peer-789"), "Third Device", System.currentTimeMillis())
        )
        
        peers.forEach { repository.upsertPeer(it) }
        
        val retrievedPeers = repository.observeKnownPeers().first()
        assertEquals(3, retrievedPeers.size)
        assertTrue(retrievedPeers.any { it.id == testPeer.id })
    }

    @Test
    fun purgeStalePeers() = runTest {
        val oldTimestamp = System.currentTimeMillis() - 10000
        val stalePeer = testPeer.copy(lastSeenUtc = oldTimestamp)
        val activePeer = Peer(
            PeerId("active-peer"),
            "Active Device",
            System.currentTimeMillis()
        )
        
        repository.upsertPeer(stalePeer)
        repository.upsertPeer(activePeer)
        
        repository.purgeStalePeers(System.currentTimeMillis() - 5000)
        
        val remainingPeers = repository.observeKnownPeers().first()
        assertEquals(1, remainingPeers.size)
        assertEquals(activePeer.id, remainingPeers[0].id)
    }

    @Test
    fun insertPeerWithNullAlias() = runTest {
        val peerWithoutAlias = testPeer.copy(alias = null)
        repository.upsertPeer(peerWithoutAlias)
        
        val peers = repository.observeKnownPeers().first()
        assertEquals(1, peers.size)
        assertNull(peers[0].alias)
    }

    @Test
    fun updatePeerAlias() = runTest {
        repository.upsertPeer(testPeer)
        
        val updatedPeer = testPeer.copy(alias = null)
        repository.upsertPeer(updatedPeer)
        
        val peers = repository.observeKnownPeers().first()
        assertNull(peers[0].alias)
    }

    @Test
    fun observeEmptyPeerList() = runTest {
        val peers = repository.observeKnownPeers().first()
        assertTrue(peers.isEmpty())
    }

    @Test
    fun purgeAllPeers() = runTest {
        val peers = listOf(
            testPeer,
            Peer(PeerId("test-peer-456"), "Second Device", System.currentTimeMillis()),
            Peer(PeerId("test-peer-789"), "Third Device", System.currentTimeMillis())
        )
        
        peers.forEach { repository.upsertPeer(it) }
        
        val futureTimestamp = System.currentTimeMillis() + 10000
        repository.purgeStalePeers(futureTimestamp)
        
        val remainingPeers = repository.observeKnownPeers().first()
        assertTrue(remainingPeers.isEmpty())
    }

    @Test
    fun upsertSamePeerMultipleTimes() = runTest {
        repeat(5) {
            repository.upsertPeer(testPeer)
        }
        
        val peers = repository.observeKnownPeers().first()
        assertEquals(1, peers.size)
        assertEquals(testPeer.id, peers[0].id)
    }
}