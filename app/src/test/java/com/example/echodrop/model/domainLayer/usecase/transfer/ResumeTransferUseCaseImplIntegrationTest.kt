package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.TransferDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.TransferLogEntity
import com.example.echodrop.model.dataLayer.impl.repository.TransferRepositoryImpl
import com.example.echodrop.model.domainLayer.model.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResumeTransferUseCaseImplIntegrationTest {

    private val dao: TransferDao = mockk()
    private lateinit var useCase: ResumeTransferUseCase
    private val paketId = PaketId("p-2")
    private val peerId  = PeerId("peer-2")

    @BeforeEach
    fun init() {
        useCase = ResumeTransferUseCase(TransferRepositoryImpl(dao))
    }

    @Test
    fun setsStateToActive() = runTest {
        coEvery { dao.findById(paketId.value, peerId.value) } returns
            TransferLogEntity(paketId.value, peerId.value,
                TransferState.PAUSED, TransferDirection.INCOMING, 50, 0)
        coJustRun { dao.upsert(any()) }

        useCase(paketId, peerId)

        coVerify { dao.upsert(match { it.state == TransferState.ACTIVE }) }
    }
} 