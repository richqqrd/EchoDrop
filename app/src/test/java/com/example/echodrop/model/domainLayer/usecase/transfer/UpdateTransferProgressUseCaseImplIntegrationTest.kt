package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.TransferDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.TransferLogEntity
import com.example.echodrop.model.dataLayer.impl.repository.TransferRepositoryImpl
import com.example.echodrop.model.domainLayer.model.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateTransferProgressUseCaseImplIntegrationTest {

    private val dao: TransferDao = mockk()
    private lateinit var useCase: UpdateTransferProgressUseCase
    private val paketId = PaketId("p-4")
    private val peerId  = PeerId("peer-4")

    @BeforeEach
    fun init() { useCase = UpdateTransferProgressUseCase(TransferRepositoryImpl(dao)) }

    @Test
    fun writesNewProgress_toDao() = runTest {
        coEvery { dao.findById(paketId.value, peerId.value) } returns
            TransferLogEntity(paketId.value, peerId.value,
                TransferState.ACTIVE, TransferDirection.OUTGOING, 10, 0)
        coJustRun { dao.upsert(any()) }

        useCase(paketId, peerId, progressPct = 42)

        coVerify { dao.upsert(match { it.progressPct == 42 }) }
    }
} 