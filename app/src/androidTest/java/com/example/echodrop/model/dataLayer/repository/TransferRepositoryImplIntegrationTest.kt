package com.example.echodrop.model.dataLayer.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PaketEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PeerEntity
import com.example.echodrop.model.dataLayer.impl.repository.TransferRepositoryImpl
import com.example.echodrop.model.domainLayer.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for TransferRepositoryImpl
 */
@RunWith(AndroidJUnit4::class)
class TransferRepositoryImplIntegrationTest {
    private lateinit var db: EchoDatabase
    private lateinit var repository: TransferRepositoryImpl

    private val testPaketId = PaketId("test-paket-123")
    private val testPeerId = PeerId("test-peer-456")

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            EchoDatabase::class.java
        ).build()
        repository = TransferRepositoryImpl(db.transferDao())

        runTest {
            db.paketDao().upsert(
                PaketEntity(
                    paketId = testPaketId.value,
                    version = 1,
                    title = "Test Paket",
                    description = "Test Description",
                    tags = listOf("test"),
                    sizeBytes = 1024L,
                    fileCount = 1,
                    ttlSeconds = 3600,
                    priority = 1,
                    hopLimit = null,
                    createdUtc = System.currentTimeMillis()
                )
            )

            db.peerDao().upsert(
                PeerEntity(
                    peerId = testPeerId.value,
                    alias = "Test Peer",
                    lastSeenUtc = System.currentTimeMillis()
                )
            )
        }
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun createAndObserveTransfer() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        
        val transfers = repository.observeTransfers().first()
        
        assertEquals(1, transfers.size)
        assertEquals(testPaketId, transfers[0].paketId)
        assertEquals(testPeerId, transfers[0].peerId)
        assertEquals(TransferState.ACTIVE, transfers[0].state)
        assertEquals(0, transfers[0].progressPct)
    }

    @Test
    fun pauseAndResumeTransfer() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        repository.pause(testPaketId, testPeerId)
        
        var transfers = repository.observeTransfers().first()
        assertEquals(TransferState.PAUSED, transfers[0].state)
        
        repository.resume(testPaketId, testPeerId)
        transfers = repository.observeTransfers().first()
        assertEquals(TransferState.ACTIVE, transfers[0].state)
    }

    @Test
    fun updateTransferProgress() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        repository.updateProgress(testPaketId, testPeerId, 50)
        
        val transfers = repository.observeTransfers().first()
        assertEquals(50, transfers[0].progressPct)
    }

    @Test
    fun cancelTransfer() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        repository.cancel(testPaketId, testPeerId)
        
        val transfers = repository.observeTransfers().first()
        assertTrue(transfers.isEmpty())
    }

    @Test
    fun multipleTransfers() = runTest {
        val secondPaketId = PaketId("test-paket-789")
        val secondPeerId = PeerId("test-peer-012")

        db.paketDao().upsert(
            PaketEntity(
                paketId = secondPaketId.value,
                version = 1,
                title = "Second Paket",
                description = "Another test",
                tags = listOf("test"),
                sizeBytes = 2048L,
                fileCount = 1,
                ttlSeconds = 3600,
                priority = 2,
                hopLimit = null,
                createdUtc = System.currentTimeMillis()
            )
        )

        db.peerDao().upsert(
            PeerEntity(
                peerId = secondPeerId.value,
                alias = "Second Peer",
                lastSeenUtc = System.currentTimeMillis()
            )
        )

        repository.startTransfer(testPaketId, testPeerId)
        repository.startTransfer(secondPaketId, secondPeerId)
        
        val transfers = repository.observeTransfers().first()
        assertEquals(2, transfers.size)
    }

    @Test
    fun startExistingTransfer() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        repository.startTransfer(testPaketId, testPeerId)
        
        val transfers = repository.observeTransfers().first()
        assertEquals(1, transfers.size)
    }

    @Test
    fun pauseNonExistentTransfer() = runTest {
        repository.pause(testPaketId, testPeerId)
        
        val transfers = repository.observeTransfers().first()
        assertTrue(transfers.isEmpty())
    }

    @Test
    fun resumeNonExistentTransfer() = runTest {
        repository.resume(testPaketId, testPeerId)
        
        val transfers = repository.observeTransfers().first()
        assertTrue(transfers.isEmpty())
    }

    @Test
    fun updateProgressForNonExistentTransfer() = runTest {
        repository.updateProgress(testPaketId, testPeerId, 50)
        
        val transfers = repository.observeTransfers().first()
        assertTrue(transfers.isEmpty())
    }

    @Test
    fun transferStateTransitions() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        
        var transfers = repository.observeTransfers().first()
        assertEquals(TransferState.ACTIVE, transfers[0].state)
        
        repository.pause(testPaketId, testPeerId)
        transfers = repository.observeTransfers().first()
        assertEquals(TransferState.PAUSED, transfers[0].state)
        
        repository.resume(testPaketId, testPeerId)
        transfers = repository.observeTransfers().first()
        assertEquals(TransferState.ACTIVE, transfers[0].state)
        
        repository.updateProgress(testPaketId, testPeerId, 100)
        transfers = repository.observeTransfers().first()
        assertEquals(100, transfers[0].progressPct)
    }

    @Test
    fun observeEmptyTransfers() = runTest {
        val transfers = repository.observeTransfers().first()
        assertTrue(transfers.isEmpty())
    }

    @Test
    fun updateProgressMultipleTimes() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        
        for (progress in 0..100 step 10) {
            repository.updateProgress(testPaketId, testPeerId, progress)
            val transfers = repository.observeTransfers().first()
            assertEquals(progress, transfers[0].progressPct)
        }
    }

    @Test
    fun updateTransferState() = runTest {
        repository.startTransfer(testPaketId, testPeerId)
        repository.updateState(testPaketId, testPeerId, TransferState.FAILED)
        
        val transfers = repository.observeTransfers().first()
        assertEquals(TransferState.FAILED, transfers[0].state)
    }

    @Test
    fun checkTransferDirection() = runTest {
        val outgoingPeerId = PeerId("out-peer-123")
        val incomingPeerId = PeerId("in-peer-456")

        db.peerDao().upsert(
            PeerEntity(
                peerId = outgoingPeerId.value,
                alias = "Outgoing Peer",
                lastSeenUtc = System.currentTimeMillis()
            )
        )

        db.peerDao().upsert(
            PeerEntity(
                peerId = incomingPeerId.value,
                alias = "Incoming Peer",
                lastSeenUtc = System.currentTimeMillis()
            )
        )

        repository.startTransfer(testPaketId, outgoingPeerId)
        repository.startTransfer(testPaketId, incomingPeerId)
        
        val transfers = repository.observeTransfers().first()
        
        val outgoingTransfer = transfers.find { it.peerId == outgoingPeerId }
        val incomingTransfer = transfers.find { it.peerId == incomingPeerId }
        
        assertEquals(TransferDirection.OUTGOING, outgoingTransfer?.direction)
        assertEquals(TransferDirection.INCOMING, incomingTransfer?.direction)
    }
}