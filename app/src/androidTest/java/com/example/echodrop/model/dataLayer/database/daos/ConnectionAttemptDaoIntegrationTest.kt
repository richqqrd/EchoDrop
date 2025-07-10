package com.example.echodrop.model.dataLayer.database.daos

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.ConnectionAttemptDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.ConnectionAttemptEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ConnectionAttemptDaoIntegrationTest {

    private lateinit var database: EchoDatabase
    private lateinit var dao: ConnectionAttemptDao

    private val device = "aa:bb:cc:dd:ee:ff"
    private val paketId = "paket-1"

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            EchoDatabase::class.java
        ).allowMainThreadQueries().build()

        dao = database.connectionAttemptDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndQueryFailedAttempts() = runBlocking {
        val now = System.currentTimeMillis()
        // 2 Fehlversuche
        dao.insert(ConnectionAttemptEntity(device, paketId, now - 1000, false))
        dao.insert(ConnectionAttemptEntity(device, paketId, now - 500,  false))
        // 1 Erfolgsversuch
        dao.insert(ConnectionAttemptEntity(device, paketId, now - 200,  true))

        val fails = dao.getFailedAttemptCount(device, paketId, now - 2_000)
        assertEquals(2, fails)
    }

    @Test
    fun deleteOlderThan_removesOldEntries() = runBlocking {
        val tOld  = System.currentTimeMillis() - 10_000
        val tKeep = System.currentTimeMillis() - 1_000

        dao.insert(ConnectionAttemptEntity(device, paketId, tOld,  false))
        dao.insert(ConnectionAttemptEntity(device, paketId, tKeep, false))

        // Nur der alte Eintrag soll verschwinden
        val removed = dao.deleteOlderThan(tKeep)
        assertEquals(1, removed)

        val remaining = dao.getFailedAttemptCount(device, paketId, tKeep - 100)
        assertEquals(1, remaining)
    }
} 