package com.example.echodrop.model.dataLayer.database.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PaketDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PaketEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Test class for the `PaketDao` data access object.
 */
@RunWith(AndroidJUnit4::class)
class PaketDaoIntegrationTest {

    private lateinit var database: EchoDatabase
    private lateinit var paketDao: PaketDao

    private val testPaketEntity1 = PaketEntity(
        paketId = "paket-123",
        version = 1,
        title = "Test Package 1",
        description = "Test description 1",
        tags = listOf("test", "first"),
        sizeBytes = 1024L,
        fileCount = 2,
        ttlSeconds = 3600,
        priority = 1,
        hopLimit = null,
        createdUtc = System.currentTimeMillis()
    )

    private val testPaketEntity2 = PaketEntity(
        paketId = "paket-456",
        version = 1,
        title = "Test Package 2",
        description = "Test description 2",
        tags = listOf("test", "second"),
        sizeBytes = 2048L,
        fileCount = 3,
        ttlSeconds = 7200,
        priority = 2,
        hopLimit = 5,
        createdUtc = System.currentTimeMillis() - 1000
    )

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EchoDatabase::class.java
        ).allowMainThreadQueries().build()

        paketDao = database.paketDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndFindById() = runBlocking {
        paketDao.upsert(testPaketEntity1)

        val retrieved = paketDao.findById(testPaketEntity1.paketId)

        assertNotNull("Retrieved paket should not be null", retrieved)
        assertEquals(testPaketEntity1.paketId, retrieved?.paketId)
        assertEquals(testPaketEntity1.title, retrieved?.title)
        assertEquals(testPaketEntity1.description, retrieved?.description)
        assertEquals(testPaketEntity1.tags, retrieved?.tags)
        assertEquals(testPaketEntity1.sizeBytes, retrieved?.sizeBytes)
        assertEquals(testPaketEntity1.fileCount, retrieved?.fileCount)
    }

    @Test
    fun findByIdReturnsNullForNonExistentId() = runBlocking {
        val result = paketDao.findById("non-existent-id")

        assertNull("Result should be null for non-existent ID", result)
    }

    @Test
    fun updateExistingPaket() = runBlocking {
        paketDao.upsert(testPaketEntity1)

        val updatedPaket = testPaketEntity1.copy(
            title = "Updated Title",
            description = "Updated description",
            priority = 3
        )
        paketDao.upsert(updatedPaket)

        val retrieved = paketDao.findById(testPaketEntity1.paketId)

        assertEquals("Updated Title", retrieved?.title)
        assertEquals("Updated description", retrieved?.description)
        assertEquals(3, retrieved?.priority)
    }

    @Test
    fun deleteById() = runBlocking {
        paketDao.upsert(testPaketEntity1)

        assertNotNull(paketDao.findById(testPaketEntity1.paketId))

        paketDao.deleteById(testPaketEntity1.paketId)

        assertNull(paketDao.findById(testPaketEntity1.paketId))
    }

    @Test
    fun observeAllReturnsAllPaketsOrderedByPriorityAndCreatedUtc() = runBlocking {
        val highPriorityPaket = testPaketEntity1.copy(
            paketId = "paket-123",
            priority = 2,
            createdUtc = System.currentTimeMillis()
        )
        
        val lowPriorityPaket = testPaketEntity2.copy(
            paketId = "paket-456",
            priority = 1,
            createdUtc = System.currentTimeMillis() + 1000
        )
    
        paketDao.upsert(lowPriorityPaket)
        paketDao.upsert(highPriorityPaket)
    
        val pakets = paketDao.observeAll().first()
    
        assertEquals(2, pakets.size)
        assertEquals(highPriorityPaket.paketId, pakets[0].paketId)
        assertEquals(lowPriorityPaket.paketId, pakets[1].paketId)
    }
    
    @Test
    fun observeAllReturnsAllPaketsSamePriorityOrderedByCreatedUtc() = runBlocking {
        val olderPaket = testPaketEntity1.copy(
            paketId = "paket-123",
            priority = 1,
            createdUtc = System.currentTimeMillis()
        )
        
        val newerPaket = testPaketEntity2.copy(
            paketId = "paket-456",
            priority = 1,
            createdUtc = System.currentTimeMillis() + 1000
        )
    
        paketDao.upsert(olderPaket)
        paketDao.upsert(newerPaket)
    
        val pakets = paketDao.observeAll().first()
    
        assertEquals(2, pakets.size)
        assertEquals(newerPaket.paketId, pakets[0].paketId)
        assertEquals(olderPaket.paketId, pakets[1].paketId)
    }

    @Test
    fun purgeExpiredDeletesExpiredPakets() = runBlocking {
        val shortTtlPaket = testPaketEntity1.copy(
            paketId = "expired-paket",
            ttlSeconds = 1,
            createdUtc = System.currentTimeMillis() - 2000
        )
        paketDao.upsert(shortTtlPaket)

        val longTtlPaket = testPaketEntity2.copy(
            paketId = "valid-paket",
            ttlSeconds = 3600
        )
        paketDao.upsert(longTtlPaket)

        val deletedCount = paketDao.purgeExpired(System.currentTimeMillis())

        assertEquals(1, deletedCount)
        assertNull(paketDao.findById(shortTtlPaket.paketId))
        assertNotNull(paketDao.findById(longTtlPaket.paketId))
    }
}