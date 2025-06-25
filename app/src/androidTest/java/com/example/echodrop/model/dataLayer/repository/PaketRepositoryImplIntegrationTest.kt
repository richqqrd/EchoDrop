package com.example.echodrop.model.dataLayer.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.database.EchoDatabase
import com.example.echodrop.model.dataLayer.repositoryImpl.PaketRepositoryImpl
import com.example.echodrop.model.domainLayer.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for PaketRepositoryImpl
 */
@RunWith(AndroidJUnit4::class)
class PaketRepositoryImplIntegrationTest {
    private lateinit var db: EchoDatabase
    private lateinit var repository: PaketRepositoryImpl

    private val testFiles = listOf(
        FileEntry(
            path = "documents/test1.pdf",
            mime = "application/pdf",
            sizeBytes = 1024L,
            orderIdx = 0
        ),
        FileEntry(
            path = "images/test2.jpg",
            mime = "image/jpeg",
            sizeBytes = 2048L,
            orderIdx = 1
        )
    )

    private val testMeta = PaketMeta(
        title = "Test Paket",
        description = "Test Description",
        tags = listOf("test", "integration"),
        ttlSeconds = 3600,
        priority = 1
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            EchoDatabase::class.java
        ).build()
        repository = PaketRepositoryImpl(db.paketDao(), db.fileEntryDao())
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insertAndRetrievePaket() = runTest {
        val paketId = repository.insert(testMeta, testFiles)

        val retrievedPaket = repository.getPaket(paketId)

        assertNotNull(retrievedPaket)
        retrievedPaket?.let { paket ->
            assertEquals(testMeta.title, paket.meta.title)
            assertEquals(testMeta.description, paket.meta.description)
            assertEquals(testMeta.tags, paket.meta.tags)
            assertEquals(testMeta.ttlSeconds, paket.meta.ttlSeconds)
            assertEquals(testMeta.priority, paket.meta.priority)
            
            assertEquals(testFiles.size, paket.files.size)
            testFiles.forEachIndexed { index, expectedFile ->
                val actualFile = paket.files[index]
                assertEquals(expectedFile.path, actualFile.path)
                assertEquals(expectedFile.mime, actualFile.mime)
                assertEquals(expectedFile.sizeBytes, actualFile.sizeBytes)
                assertEquals(expectedFile.orderIdx, actualFile.orderIdx)
            }
        }
    }

    @Test
    fun observeInbox() = runTest {
        val paketId1 = repository.insert(testMeta, testFiles)
        val paketId2 = repository.insert(
            testMeta.copy(
                title = "Second Paket",
                description = "Another test paket"
            ),
            testFiles
        )

        val inboxPakets = repository.observeInbox().first()

        assertEquals(2, inboxPakets.size)
        assertTrue(inboxPakets.any { it.id == paketId1 })
        assertTrue(inboxPakets.any { it.id == paketId2 })
    }

    @Test
    fun updateMetaData() = runTest {
        val paketId = repository.insert(testMeta, testFiles)

        val newTtl = 7200
        val newPriority = 2
        repository.updateMeta(paketId, newTtl, newPriority)

        val updatedPaket = repository.getPaket(paketId)
        assertNotNull(updatedPaket)
        assertEquals(newTtl, updatedPaket?.meta?.ttlSeconds)
        assertEquals(newPriority, updatedPaket?.meta?.priority)
    }

    @Test
    fun deletePaket() = runTest {
        val paketId = repository.insert(testMeta, testFiles)

        repository.delete(paketId)

        val deletedPaket = repository.getPaket(paketId)
        assertNull(deletedPaket)

        val inboxPakets = repository.observeInbox().first()
        assertTrue(inboxPakets.none { it.id == paketId })
    }

    @Test
    fun purgeExpiredPakets() = runTest {
        val shortTtlMeta = testMeta.copy(ttlSeconds = 1)
        val paketId = repository.insert(shortTtlMeta, testFiles)

        val futureTimestamp = System.currentTimeMillis() + 2000 

        val deletedCount = repository.purgeExpire(futureTimestamp)

        assertEquals(1, deletedCount)
        val deletedPaket = repository.getPaket(paketId)
        assertNull(deletedPaket)
    }

    @Test
    fun insertPaketWithNoFiles() = runTest {
        val paketId = repository.insert(testMeta, emptyList())

        val retrievedPaket = repository.getPaket(paketId)

        assertNotNull(retrievedPaket)
        retrievedPaket?.let { paket ->
            assertEquals(testMeta.title, paket.meta.title)
            assertTrue(paket.files.isEmpty())
        }
    }

    @Test
    fun insertPaketWithLargeFileList() = runTest {
        val largeFileList = (1..1000).map { index ->
            FileEntry(
                path = "file$index.txt",
                mime = "text/plain",
                sizeBytes = 100L,
                orderIdx = index
            )
        }

        val paketId = repository.insert(testMeta, largeFileList)

        val retrievedPaket = repository.getPaket(paketId)

        assertNotNull(retrievedPaket)
        retrievedPaket?.let { paket ->
            assertEquals(1000, paket.files.size)
            assertEquals(100_000L, paket.files.sumOf { it.sizeBytes }) // Gesamtgröße überprüfen
        }
    }

    @Test
    fun updateNonExistentPaket() = runTest {
        val nonExistentId = PaketId("non-existent")
        repository.updateMeta(nonExistentId, 7200, 2)

        val nonExistentPaket = repository.getPaket(nonExistentId)
        assertNull(nonExistentPaket)
    }

    @Test
    fun insertPaketWithDuplicateFiles() = runTest {
        val duplicateFiles = listOf(
            FileEntry("test.txt", "text/plain", 100L, 0),
            FileEntry("test.txt", "text/plain", 100L, 1)
        )

        val paketId = repository.insert(testMeta, duplicateFiles)

        val retrievedPaket = repository.getPaket(paketId)

        assertNotNull(retrievedPaket)
        retrievedPaket?.let { paket ->
            assertEquals(2, paket.files.size)
            assertEquals(0, paket.files[0].orderIdx)
            assertEquals(1, paket.files[1].orderIdx)
            assertEquals("test.txt", paket.files[0].path)
            assertEquals("test.txt", paket.files[1].path)
        }
    }

    @Test
    fun testCascadingDelete() = runTest {
        val paketId1 = repository.insert(testMeta, testFiles)
        val paketId2 = repository.insert(
            testMeta.copy(title = "Second Paket"),
            testFiles
        )

        repository.delete(paketId1)

        val remainingPakets = repository.observeInbox().first()
        assertEquals(1, remainingPakets.size)
        assertEquals(paketId2, remainingPakets[0].id)

        val paket2 = repository.getPaket(paketId2)
        assertNotNull(paket2)
        assertEquals(testFiles.size, paket2?.files?.size)
    }
}