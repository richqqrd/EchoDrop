package com.example.echodrop.model.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.database.EchoDatabase
import com.example.echodrop.model.dataLayer.database.daos.PeerDao
import com.example.echodrop.model.dataLayer.database.entities.PeerEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test class for the `PeerDao` data access object.
 */
@RunWith(AndroidJUnit4::class)
class PeerDaoTest {

    private lateinit var database: EchoDatabase
    private lateinit var peerDao: PeerDao

    private val testPeer1 = PeerEntity(
        peerId = "peer-123",
        alias = "Test Device 1",
        lastSeenUtc = System.currentTimeMillis()
    )

    private val testPeer2 = PeerEntity(
        peerId = "peer-456",
        alias = "Test Device 2",
        lastSeenUtc = System.currentTimeMillis() - 1000
    )

    private val testPeer3 = PeerEntity(
        peerId = "peer-789",
        alias = null,
        lastSeenUtc = System.currentTimeMillis() - 3600000
    )

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EchoDatabase::class.java
        ).allowMainThreadQueries().build()

        peerDao = database.peerDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndObservePeers() = runBlocking {
        peerDao.upsert(testPeer1)
        peerDao.upsert(testPeer2)
        peerDao.upsert(testPeer3)

        val peers = peerDao.observeAll().first()

        assertEquals(3, peers.size)
        assertEquals(testPeer1.peerId, peers[0].peerId)
        assertEquals(testPeer2.peerId, peers[1].peerId)
        assertEquals(testPeer3.peerId, peers[2].peerId)
    }

    @Test
    fun updateExistingPeer() = runBlocking {
        peerDao.upsert(testPeer1)

        val updatedPeer = testPeer1.copy(
            alias = "Updated Device Name",
            lastSeenUtc = System.currentTimeMillis() + 5000
        )
        peerDao.upsert(updatedPeer)

        val retrievedPeers = peerDao.observeAll().first()
        val retrievedPeer = retrievedPeers.first { it.peerId == testPeer1.peerId }

        assertEquals("Updated Device Name", retrievedPeer.alias)
        assertEquals(updatedPeer.lastSeenUtc, retrievedPeer.lastSeenUtc)
    }

    @Test
    fun purgeStaleDeletesPeersOlderThanCutoff() = runBlocking {
        val now = System.currentTimeMillis()

        val recentPeer = PeerEntity(
            peerId = "recent-peer",
            alias = "Recent Device",
            lastSeenUtc = now
        )

        val oldPeer = PeerEntity(
            peerId = "old-peer",
            alias = "Old Device",
            lastSeenUtc = now - 86400000
        )

        val veryOldPeer = PeerEntity(
            peerId = "very-old-peer",
            alias = "Very Old Device",
            lastSeenUtc = now - 172800000
        )

        peerDao.upsert(recentPeer)
        peerDao.upsert(oldPeer)
        peerDao.upsert(veryOldPeer)

        val cutoffTime = now - 43200000
        val deletedCount = peerDao.purgeStale(cutoffTime)

        val remainingPeers = peerDao.observeAll().first()

        assertEquals(2, deletedCount)
        assertEquals(1, remainingPeers.size)
        assertTrue(remainingPeers.any { it.peerId == "recent-peer" })
        assertFalse(remainingPeers.any { it.peerId == "old-peer" })
        assertFalse(remainingPeers.any { it.peerId == "very-old-peer" })
    }

    @Test
    fun insertPeerWithNullAlias() = runBlocking {
        val peerWithNullAlias = PeerEntity(
            peerId = "peer-null-alias",
            alias = null,
            lastSeenUtc = System.currentTimeMillis()
        )
        peerDao.upsert(peerWithNullAlias)

        val peers = peerDao.observeAll().first()
        val retrievedPeer = peers.first { it.peerId == "peer-null-alias" }

        assertNull(retrievedPeer.alias)
    }
}