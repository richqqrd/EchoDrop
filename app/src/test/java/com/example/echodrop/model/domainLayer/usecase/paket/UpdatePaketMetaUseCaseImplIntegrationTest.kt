package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PaketDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PaketEntity
import com.example.echodrop.model.dataLayer.impl.repository.PaketRepositoryImpl
import com.example.echodrop.model.domainLayer.model.PaketId
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdatePaketMetaUseCaseImplIntegrationTest {

    private val paketDao: PaketDao = mockk()
    private lateinit var useCase: UpdatePaketMetaUseCase
    private val paketId = PaketId("p-321")

    @BeforeEach
    fun init() {
        val dummyFileDao = mockk<com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao>(relaxed = true)
        val repo = PaketRepositoryImpl(paketDao, dummyFileDao)
        useCase = UpdatePaketMetaUseCase(repo)
    }

    @Test
    fun updatesTtlAndPriority_onEntity() = runTest {
        // Arrange – DAO liefert vorhandenes Entity
        val original = PaketEntity(
            paketId = paketId.value, version = 1, title = "t", description = null,
            tags = emptyList(), sizeBytes = 0, fileCount = 0,
            ttlSeconds = 100, priority = 1, hopLimit = null,
            currentHopCount = 0, createdUtc = 0
        )
        coEvery { paketDao.findById(paketId.value) } returns original
        coJustRun { paketDao.upsert(any()) }

        // Act
        useCase(paketId, ttlSeconds = 500, priority = 3)

        // Assert – upsert mit aktualisierten Werten
        coVerify {
            paketDao.upsert(match { it.ttlSeconds == 500 && it.priority == 3 })
        }
    }
} 