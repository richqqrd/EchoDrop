package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PaketDao
import com.example.echodrop.model.dataLayer.impl.repository.PaketRepositoryImpl
import com.example.echodrop.model.domainLayer.model.PaketId
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeletePaketUseCaseImplIntegrationTest {

    private val paketDao: PaketDao = mockk(relaxed = true)
    private lateinit var useCase: DeletePaketUseCase
    private val paketId = PaketId("p-123")

    @BeforeEach
    fun init() {
        val dummyFileDao = mockk<com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao>(relaxed = true)
        val repo = PaketRepositoryImpl(paketDao, dummyFileDao)
        useCase = DeletePaketUseCase(repo)
    }

    @Test
    fun invokesDaoDeleteById() = runTest {
        // Act
        useCase(paketId)

        // Assert – Repository hat richtigen DAO-Call ausgeführt
        coVerify(exactly = 1) { paketDao.deleteById(paketId.value) }
    }
} 