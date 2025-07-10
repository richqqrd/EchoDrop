package com.example.echodrop.model.dataLayer.database.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.ChunkDao
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PaketDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.ChunkEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PaketEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test class for the `ChunkDao` data access object.
 */
@RunWith(AndroidJUnit4::class)
class ChunkDaoIntegrationTest {

    private lateinit var database: EchoDatabase
    private lateinit var chunkDao: ChunkDao
    private lateinit var paketDao: PaketDao
    private lateinit var fileEntryDao: FileEntryDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EchoDatabase::class.java
        ).allowMainThreadQueries().build()

        chunkDao = database.chunkDao()
        fileEntryDao = database.fileEntryDao()
        paketDao = database.paketDao()

        runBlocking {
            val paketEntity = PaketEntity(
                paketId = "paket-123",
                version = 1,
                title = "Test Package",
                description = "Test description",
                tags = emptyList(),
                sizeBytes = 1024L,
                fileCount = 1,
                ttlSeconds = 3600,
                priority = 1,
                hopLimit = null,
                createdUtc = System.currentTimeMillis()
            )
            paketDao.upsert(paketEntity)

            val fileEntry = FileEntryEntity(
                fileId = "file-123",
                paketOwnerId = "paket-123",
                path = "test/file.txt",
                mime = "text/plain",
                sizeBytes = 1024L,
                orderIdx = 0
            )
            fileEntryDao.insertAll(listOf(fileEntry))
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveChunks() = runBlocking {
        val chunks = listOf(
            ChunkEntity(
                chunkId = "chunk-1",
                fileId = "file-123",
                offset = 0L,
                size = 512,
                completed = false
            ),
            ChunkEntity(
                chunkId = "chunk-2",
                fileId = "file-123",
                offset = 512L,
                size = 512,
                completed = true
            )
        )

        chunkDao.upsertChunks(chunks)
        val retrievedChunks = chunkDao.findByFile("file-123")

        assertEquals(2, retrievedChunks.size)
        assertEquals(chunks[0].chunkId, retrievedChunks[0].chunkId)
        assertEquals(chunks[1].chunkId, retrievedChunks[1].chunkId)
        assertEquals(0L, retrievedChunks[0].offset) // Should be ordered by offset
        assertEquals(512L, retrievedChunks[1].offset)
    }

    @Test
    fun deleteChunksByFile() = runBlocking {
        val chunks = listOf(
            ChunkEntity(
                chunkId = "chunk-1",
                fileId = "file-123",
                offset = 0L,
                size = 512,
                completed = false
            ),
            ChunkEntity(
                chunkId = "chunk-2",
                fileId = "file-123",
                offset = 512L,
                size = 512,
                completed = true
            )
        )
        chunkDao.upsertChunks(chunks)

        val deletedCount = chunkDao.deleteByFile("file-123")
        val remainingChunks = chunkDao.findByFile("file-123")

        assertEquals(2, deletedCount)
        assertTrue(remainingChunks.isEmpty())
    }
}