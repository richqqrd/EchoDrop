package com.example.echodrop.model.domainLayer.usecase.file

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.impl.repository.FileRepositoryImpl
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketId
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InsertFilesUseCaseImplIntegrationTest {

    private val fileDao: FileEntryDao = mockk(relaxed = true)
    private lateinit var useCase: InsertFilesUseCase
    private val paketId = PaketId("p-999")

    @BeforeEach
    fun init() {
        val repo = FileRepositoryImpl(fileDao)
        useCase = InsertFilesUseCase(repo)
    }

    @Test
    fun passesMappedEntities_toDaoInsertAll() = runTest {
        // Arrange
        val files = listOf(
            FileEntry("/tmp/a.txt", "text/plain", 123, 0),
            FileEntry("/tmp/b.jpg", "image/jpeg", 456, 1)
        )

        // Act
        useCase(paketId, files)

        // Assert – DAO wurde aufgerufen
        coVerify(exactly = 1) { fileDao.insertAll(match { it.size == 2 }) }
    }
} 