package com.example.echodrop.model.dataLayer.database.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.model.TransferDirection
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PaketDao
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PeerDao
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.TransferDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PaketEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PeerEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.TransferLogEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test class for the `TransferDao` data access object.
 */
@RunWith(AndroidJUnit4::class)
class TransferDaoIntegrationTest {

    private lateinit var database: EchoDatabase
    private lateinit var transferDao: TransferDao
    private lateinit var paketDao: PaketDao
    private lateinit var peerDao: PeerDao

    private val testPaketId = "paket-123"
    private val testPeerId = "peer-456"

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EchoDatabase::class.java
        ).allowMainThreadQueries().build()

        transferDao = database.transferDao()
        paketDao = database.paketDao()
        peerDao = database.peerDao()

        runBlocking {
            val paketEntity = PaketEntity(
                paketId = testPaketId,
                version = 1,
                title = "Test Package",
                description = "Test description",
                tags = listOf("test"),
                sizeBytes = 1024L,
                fileCount = 1,
                ttlSeconds = 3600,
                priority = 1,
                hopLimit = null,
                createdUtc = System.currentTimeMillis()
            )
            paketDao.upsert(paketEntity)

            val peerEntity = PeerEntity(
                peerId = testPeerId,
                alias = "Test Peer",
                lastSeenUtc = System.currentTimeMillis()
            )
            peerDao.upsert(peerEntity)
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndObserveTransferLog() = runBlocking {
        val transferLog = TransferLogEntity(
            paketId = testPaketId,
            peerId = testPeerId,
            state = TransferState.QUEUED,
            direction = TransferDirection.OUTGOING,
            progressPct = 0,
            lastUpdateUtc = System.currentTimeMillis()
        )

        transferDao.upsert(transferLog)
        val logs = transferDao.observeAll().first()

        assertEquals(1, logs.size)
        assertEquals(testPaketId, logs[0].paketId)
        assertEquals(testPeerId, logs[0].peerId)
        assertEquals(TransferState.QUEUED, logs[0].state)
    }

    @Test
    fun findByIdReturnsCorrectTransferLog() = runBlocking {
        val transferLog = TransferLogEntity(
            paketId = testPaketId,
            peerId = testPeerId,
            state = TransferState.ACTIVE,
            direction = TransferDirection.OUTGOING,
            progressPct = 50,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(transferLog)

        val retrievedLog = transferDao.findById(testPaketId, testPeerId)

        assertNotNull(retrievedLog)
        assertEquals(transferLog.paketId, retrievedLog?.paketId)
        assertEquals(transferLog.peerId, retrievedLog?.peerId)
        assertEquals(transferLog.state, retrievedLog?.state)
        assertEquals(transferLog.progressPct, retrievedLog?.progressPct)
    }

    @Test
    fun findByIdReturnsNullForNonExistentLog() = runBlocking {
        val result = transferDao.findById("non-existent-paket", "non-existent-peer")

        assertNull(result)
    }

    @Test
    fun updateExistingTransferLog() = runBlocking {
        val initialLog = TransferLogEntity(
            paketId = testPaketId,
            peerId = testPeerId,
            state = TransferState.QUEUED,
            direction = TransferDirection.OUTGOING,
            progressPct = 0,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(initialLog)

        val updatedLog = initialLog.copy(
            state = TransferState.ACTIVE,
            progressPct = 25,
            lastUpdateUtc = System.currentTimeMillis() + 1000
        )
        transferDao.upsert(updatedLog)

        val retrievedLog = transferDao.findById(testPaketId, testPeerId)

        assertEquals(TransferState.ACTIVE, retrievedLog?.state)
        assertEquals(25, retrievedLog?.progressPct)
    }

    @Test
    fun deleteTransferLog() = runBlocking {
        val transferLog = TransferLogEntity(
            paketId = testPaketId,
            peerId = testPeerId,
            state = TransferState.ACTIVE,
            direction = TransferDirection.OUTGOING,
            progressPct = 50,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(transferLog)

        assertNotNull(transferDao.findById(testPaketId, testPeerId))

        val deletedCount = transferDao.delete(testPaketId, testPeerId)

        assertEquals(1, deletedCount)
        assertNull(transferDao.findById(testPaketId, testPeerId))
    }

    @Test
    fun cascadeDeleteWhenParentDeleted() = runBlocking {
        val transferLog = TransferLogEntity(
            paketId = testPaketId,
            peerId = testPeerId,
            state = TransferState.ACTIVE,
            direction = TransferDirection.OUTGOING,
            progressPct = 50,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(transferLog)

        assertNotNull(transferDao.findById(testPaketId, testPeerId))

        paketDao.deleteById(testPaketId)

        assertNull(transferDao.findById(testPaketId, testPeerId))
    }

    @Test
    fun foreignKeyConstraintPreventsOrphanedEntries() = runBlocking {
        try {
            val orphanedLog = TransferLogEntity(
                paketId = "non-existent-paket",
                peerId = "non-existent-peer",
                state = TransferState.QUEUED,
                direction = TransferDirection.OUTGOING,
                progressPct = 0,
                lastUpdateUtc = System.currentTimeMillis()
            )
            transferDao.upsert(orphanedLog)

            fail("Should have thrown a foreign key constraint exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("FOREIGN KEY constraint failed") ?: false)
        }
    }
}