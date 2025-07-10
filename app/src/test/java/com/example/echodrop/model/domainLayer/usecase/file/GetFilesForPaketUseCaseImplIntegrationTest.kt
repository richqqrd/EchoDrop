package com.example.echodrop.model.domainLayer.usecase.file

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.impl.repository.FileRepositoryImpl
import com.example.echodrop.model.domainLayer.model.PaketId
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetFilesForPaketUseCaseImplIntegrationTest {

    private val fileDao: FileEntryDao = mockk()
    private lateinit var useCase: GetFilesForPaketUseCase
    private val paketId = PaketId("p-99")

    @BeforeEach
    fun setUp() {
        val repo  = FileRepositoryImpl(fileDao)
        useCase   = GetFilesForPaketUseCase(repo)
    }

    @Test
    fun returnsDomainFiles_fromDaoEntities() = runTest {
        // Arrange – DAO liefert Entity-Liste
        val entity = FileEntryEntity("f1", paketId.value, "/tmp/x.txt",
                                     "text/plain", 123, 0)
        coEvery { fileDao.findByPaket(paketId.value) } returns listOf(entity)

        // Act
        val result = useCase(paketId)

        // Assert
        assertEquals(1, result.size)
        assertEquals("/tmp/x.txt", result.first().path)
    }
} 