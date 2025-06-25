package com.example.echodrop.model.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.database.EchoDatabase
import com.example.echodrop.model.dataLayer.database.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.database.daos.PaketDao
import com.example.echodrop.model.dataLayer.database.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.database.entities.PaketEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test class for the `FileEntryDao` data access object.
 */
@RunWith(AndroidJUnit4::class)
class FileEntryDaoIntegrationTest {

    private lateinit var database: EchoDatabase
    private lateinit var fileEntryDao: FileEntryDao
    private lateinit var paketDao: PaketDao

    private val testPaketId = "paket-123"
    private val testPaketEntity = PaketEntity(
        paketId = testPaketId,
        version = 1,
        title = "Test Package",
        description = "Test description",
        tags = listOf("test"),
        sizeBytes = 2048L,
        sha256 = "test-hash",
        fileCount = 2,
        ttlSeconds = 3600,
        priority = 1,
        hopLimit = null,
        manifestHash = "test-manifest-hash",
        createdUtc = System.currentTimeMillis()
    )

    private val testFileEntries = listOf(
        FileEntryEntity(
            fileId = "file-1",
            paketOwnerId = testPaketId,
            path = "documents/doc1.txt",
            mime = "text/plain",
            sizeBytes = 1024L,
            orderIdx = 0
        ),
        FileEntryEntity(
            fileId = "file-2",
            paketOwnerId = testPaketId,
            path = "images/image1.jpg",
            mime = "image/jpeg",
            sizeBytes = 1024L,
            orderIdx = 1
        )
    )

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EchoDatabase::class.java
        ).allowMainThreadQueries().build()

        fileEntryDao = database.fileEntryDao()
        paketDao = database.paketDao()

        runBlocking {
            paketDao.upsert(testPaketEntity)
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveFileEntries() = runBlocking {
        fileEntryDao.insertAll(testFileEntries)

        val retrievedEntries = fileEntryDao.findByPaket(testPaketId)

        assertEquals(2, retrievedEntries.size)
        assertEquals("documents/doc1.txt", retrievedEntries[0].path)
        assertEquals("images/image1.jpg", retrievedEntries[1].path)
        assertEquals(0, retrievedEntries[0].orderIdx)
        assertEquals(1, retrievedEntries[1].orderIdx)
    }

    @Test
    fun deleteFileEntriesByPaketId() = runBlocking {
        fileEntryDao.insertAll(testFileEntries)

        val deletedCount = fileEntryDao.deleteByPaket(testPaketId)

        val remainingEntries = fileEntryDao.findByPaket(testPaketId)

        assertEquals(2, deletedCount)
        assertTrue(remainingEntries.isEmpty())
    }

    @Test
    fun replaceExistingFileEntries() = runBlocking {
        fileEntryDao.insertAll(testFileEntries)

        val updatedEntry = testFileEntries[0].copy(
            path = "documents/updated.txt",
            mime = "text/markdown",
            sizeBytes = 2048L
        )
        fileEntryDao.insertAll(listOf(updatedEntry))

        val retrievedEntries = fileEntryDao.findByPaket(testPaketId)
            .sortedBy { it.fileId }

        assertEquals(2, retrievedEntries.size)
        assertEquals("documents/updated.txt", retrievedEntries.first { it.fileId == "file-1" }.path)
        assertEquals("text/markdown", retrievedEntries.first { it.fileId == "file-1" }.mime)
        assertEquals(2048L, retrievedEntries.first { it.fileId == "file-1" }.sizeBytes)
    }

    @Test
    fun noFileEntriesForNonExistentPaket() = runBlocking {
        val retrievedEntries = fileEntryDao.findByPaket("non-existent-paket")

        assertTrue(retrievedEntries.isEmpty())
    }

    @Test
    fun foreignKeyConstraintPreventsOrphanedEntries() = runBlocking {
        val orphanedEntry = FileEntryEntity(
            fileId = "orphaned-file",
            paketOwnerId = "non-existent-paket",
            path = "orphaned/file.txt",
            mime = "text/plain",
            sizeBytes = 1024L,
            orderIdx = 0
        )

        try {
            fileEntryDao.insertAll(listOf(orphanedEntry))
            fail("Should have thrown a foreign key constraint exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("FOREIGN KEY constraint failed") ?: false)
        }
    }
}