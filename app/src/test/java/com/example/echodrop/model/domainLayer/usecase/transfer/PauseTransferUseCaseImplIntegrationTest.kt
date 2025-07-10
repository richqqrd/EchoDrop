package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.TransferDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.TransferLogEntity
import com.example.echodrop.model.dataLayer.impl.repository.TransferRepositoryImpl
import com.example.echodrop.model.domainLayer.model.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PauseTransferUseCaseImplIntegrationTest {

    private val transferDao: TransferDao = mockk()
    private lateinit var useCase: PauseTransferUseCase
    private val paketId = PaketId("p-1")
    private val peerId  = PeerId("peer-1")

    @BeforeEach
    fun init() {
        val repo = TransferRepositoryImpl(transferDao)
        useCase  = PauseTransferUseCase(repo)
    }

    @Test
    fun setsStateToPaused_viaDaoUpsert() = runTest {
        // Arrange – vorhandenen Log simulieren
        val entity = TransferLogEntity(
            paketId.value, peerId.value,
            TransferState.ACTIVE, TransferDirection.OUTGOING,
            progressPct = 10, lastUpdateUtc = 0
        )
        coEvery { transferDao.findById(paketId.value, peerId.value) } returns entity
        coJustRun { transferDao.upsert(any()) }

        // Act
        useCase(paketId, peerId)

        // Assert
        coVerify {
            transferDao.upsert(match { it.state == TransferState.PAUSED })
        }
    }
} 