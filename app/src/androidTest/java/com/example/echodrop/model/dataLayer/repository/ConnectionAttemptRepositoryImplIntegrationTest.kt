package com.example.echodrop.model.dataLayer.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.impl.repository.ConnectionAttemptRepositoryImpl
import com.example.echodrop.model.domainLayer.model.PaketId
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ConnectionAttemptRepositoryImplIntegrationTest {

    private lateinit var db: EchoDatabase
    private lateinit var repo: ConnectionAttemptRepositoryImpl

    private val device = "aa:bb:cc:dd:ee:ff"
    private val paketId = PaketId("paket-42")

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, EchoDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repo = ConnectionAttemptRepositoryImpl(db.connectionAttemptDao())
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun trackAttempt_persistsData_andFailedCountIsCorrect() = runTest {
        // 2 Fehl- und 1 Erfolgsversuch protokollieren
        repo.trackAttempt(device, paketId, successful = false)
        repo.trackAttempt(device, paketId, successful = false)
        repo.trackAttempt(device, paketId, successful = true)

        val fails = repo.getFailedAttemptCount(
            device,
            paketId,
            minTimestamp = System.currentTimeMillis() - 60_000
        )
        assertEquals(2, fails)
    }

    @Test
    fun cleanupOldAttempts_removesOldRows() = runTest {
        val oldTs = System.currentTimeMillis() - 120_000
        val okTs  = System.currentTimeMillis()

        // Direkt über DAO zwei Zeilen einfügen (eine alt, eine neu)
        db.connectionAttemptDao().insert(
            com.example.echodrop.model.dataLayer.datasource.persistence.entities.ConnectionAttemptEntity(
                device, paketId.value, oldTs, false
            )
        )
        db.connectionAttemptDao().insert(
            com.example.echodrop.model.dataLayer.datasource.persistence.entities.ConnectionAttemptEntity(
                device, paketId.value, okTs, false
            )
        )

        // Aufräumen
        repo.cleanupOldAttempts(okTs - 1)

        val remaining = repo.getFailedAttemptCount(device, paketId, 0)
        assertEquals(1, remaining)   // nur der neue Eintrag ist übrig
    }
} 