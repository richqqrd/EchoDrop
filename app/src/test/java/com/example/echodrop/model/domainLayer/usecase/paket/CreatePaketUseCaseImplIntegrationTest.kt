package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PaketDao
import com.example.echodrop.model.dataLayer.impl.repository.PaketRepositoryImpl
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketMeta
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreatePaketUseCaseImplIntegrationTest {

    /* ---------- DAO-Mocks ---------- */
    private val paketDao: PaketDao       = mockk(relaxed = true)
    private val fileDao : FileEntryDao   = mockk(relaxed = true)

    /* ---------- System Under Test ---------- */
    private lateinit var useCase: CreatePaketUseCase

    @BeforeEach
    fun setUp() {
        val repo = PaketRepositoryImpl(paketDao, fileDao)
        useCase  = CreatePaketUseCase(repo)
    }

    @Test
    fun insertsPaket_andFiles_viaRepositoryImpl() = runTest {
        val meta  = PaketMeta("Titel-A", null, emptyList(), 3600, 1)
        val files = listOf<FileEntry>()

        val captured = slot<com.example.echodrop.model.dataLayer.datasource.persistence.entities.PaketEntity>()
        coEvery { paketDao.upsert(capture(captured)) } returns Unit

        val id = useCase(meta, files)

        coVerify(exactly = 1) { paketDao.upsert(any()) }
        coVerify(exactly = 1) { fileDao.insertAll(any()) }

        assertEquals(captured.captured.paketId, id.value)
    }
} 