package com.example.echodrop.model.dataLayer.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.database.EchoDatabase
import com.example.echodrop.model.dataLayer.database.entities.PaketEntity
import com.example.echodrop.model.dataLayer.repositoryImpl.FileRepositoryImpl
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketId
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration test for FileRepositoryImpl
 */
@RunWith(AndroidJUnit4::class)
class FileRepositoryIntegrationTest {
    private lateinit var db: EchoDatabase
    private lateinit var repository: FileRepositoryImpl

    private val testPaketId = PaketId("test-paket-123")
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
        ),
        FileEntry(
            path = "documents/test3.txt",
            mime = "text/plain",
            sizeBytes = 512L,
            orderIdx = 2
        )
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            EchoDatabase::class.java
        ).build()
        repository = FileRepositoryImpl(db.fileEntryDao())

        runTest {
            db.paketDao().upsert(
                PaketEntity(
                    paketId = testPaketId.value,
                    version = 1,
                    title = "Test Paket",
                    description = "Test Description",
                    tags = listOf("test"),
                    sizeBytes = 0L,
                    sha256 = "test-hash",
                    fileCount = 0,
                    ttlSeconds = 3600,
                    priority = 1,
                    hopLimit = null,
                    manifestHash = "test-manifest",
                    createdUtc = System.currentTimeMillis()
                )
            )
        }
    }

    @After
    fun cleanup() {
        db.close()
    }

    @Test
    fun insertAndRetrieveFiles() = runTest {
        repository.insertAll(testPaketId, testFiles)

       val retrievedFiles = repository.getFilesFor(testPaketId)

        assertEquals(testFiles.size, retrievedFiles.size)

        testFiles.forEachIndexed { index, expectedFile ->
            val retrievedFile = retrievedFiles[index]
            assertEquals(expectedFile.path, retrievedFile.path)
            assertEquals(expectedFile.mime, retrievedFile.mime)
            assertEquals(expectedFile.sizeBytes, retrievedFile.sizeBytes)
            assertEquals(expectedFile.orderIdx, retrievedFile.orderIdx)
        }
    }

    @Test
    fun deleteFilesForPaket() = runTest {
        repository.insertAll(testPaketId, testFiles)

        repository.deleteFor(testPaketId)

        val remainingFiles = repository.getFilesFor(testPaketId)
        assertTrue(remainingFiles.isEmpty())
    }

    @Test
    fun insertFilesForMultiplePakets() = runTest {
        val secondPaketId = PaketId("test-paket-456")

        db.paketDao().upsert(
            PaketEntity(
                paketId = secondPaketId.value,
                version = 1,
                title = "Second Test Paket",
                description = "Test Description",
                tags = listOf("test"),
                sizeBytes = 0L,
                sha256 = "test-hash",
                fileCount = 0,
                ttlSeconds = 3600,
                priority = 1,
                hopLimit = null,
                manifestHash = "test-manifest",
                createdUtc = System.currentTimeMillis()
            )
        )

        repository.insertAll(testPaketId, testFiles)
        repository.insertAll(secondPaketId, testFiles)

        val firstPaketFiles = repository.getFilesFor(testPaketId)
        val secondPaketFiles = repository.getFilesFor(secondPaketId)

        assertEquals(testFiles.size, firstPaketFiles.size)
        assertEquals(testFiles.size, secondPaketFiles.size)

        firstPaketFiles.forEach { file ->
            assertTrue(testFiles.any { it.path == file.path })
        }
        secondPaketFiles.forEach { file ->
            assertTrue(testFiles.any { it.path == file.path })
        }
    }

    @Test
    fun insertEmptyFileList() = runTest {
        repository.insertAll(testPaketId, emptyList())

        val retrievedFiles = repository.getFilesFor(testPaketId)
        assertTrue(retrievedFiles.isEmpty())
    }

    @Test
    fun getFilesForNonExistentPaket() = runTest {
        val nonExistentPaketId = PaketId("non-existent")

        val retrievedFiles = repository.getFilesFor(nonExistentPaketId)

        assertTrue(retrievedFiles.isEmpty())
    }

    @Test
    fun insertDuplicateFiles() = runTest {
        val duplicateFiles = listOf(
            FileEntry("test.txt", "text/plain", 100L, 0),
            FileEntry("test.txt", "text/plain", 100L, 1)
        )

        repository.insertAll(testPaketId, duplicateFiles)

        val retrievedFiles = repository.getFilesFor(testPaketId)

        assertEquals(2, retrievedFiles.size)
        assertEquals(0, retrievedFiles[0].orderIdx)
        assertEquals(1, retrievedFiles[1].orderIdx)
    }

    @Test
    fun testLargeFileList() = runTest {
        val largeFileList = (1..1000).map { index ->
            FileEntry(
                path = "file$index.txt",
                mime = "text/plain",
                sizeBytes = 100L,
                orderIdx = index
            )
        }

        repository.insertAll(testPaketId, largeFileList)

        val retrievedFiles = repository.getFilesFor(testPaketId)

        assertEquals(1000, retrievedFiles.size)
        retrievedFiles.forEachIndexed { index, file ->
            assertEquals(index + 1, file.orderIdx)
            assertEquals("file${index + 1}.txt", file.path)
        }
    }
}