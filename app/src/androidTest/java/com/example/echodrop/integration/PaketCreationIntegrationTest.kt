package com.example.echodrop.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.impl.repository.PaketRepositoryImpl
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.usecase.paket.CreatePaketUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows

/**
 * Integration test for Paket creation flow:
 * Presentation (UseCase) → Domain (Repository Interface) → Data (Repository Implementation + DAOs)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PaketCreationIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var database: EchoDatabase

    @Inject  
    lateinit var paketRepository: PaketRepositoryImpl

    private lateinit var createPaketUseCase: CreatePaketUseCase

    @Before
    fun setup() {
        hiltRule.inject()
        createPaketUseCase = CreatePaketUseCase(paketRepository)
    }

    @Test
    fun createPaket_fullIntegrationFlow_persistsCorrectly() = runTest {
        // Arrange: Presentation Layer Data
        val meta = PaketMeta(
            title = "Integration Test Paket",
            description = "Testing full flow through all layers",
            tags = listOf("integration", "test"),
            ttlSeconds = 3600,
            priority = 5
        )
        val files = listOf(
            FileEntry(
                path = "/test/file1.txt",
                mime = "text/plain",
                sizeBytes = 1024,
                orderIdx = 0
            ),
            FileEntry(
                path = "/test/file2.jpg", 
                mime = "image/jpeg",
                sizeBytes = 2048,
                orderIdx = 1
            )
        )

        // Act: Execute through all layers
        // Presentation → Domain → Data
        val paketId = createPaketUseCase(meta, files)

        // Assert: Verify data persistence in Data Layer
        val persistedPaket = database.paketDao().getPaket(paketId.value)
        assertThat(persistedPaket).isNotNull()
        assertThat(persistedPaket!!.title).isEqualTo("Integration Test Paket")
        assertThat(persistedPaket.priority).isEqualTo(5)

        val persistedFiles = database.fileEntryDao().getFilesForPaket(paketId.value)
        assertThat(persistedFiles).hasSize(2)
        assertThat(persistedFiles[0].path).isEqualTo("/test/file1.txt")
        assertThat(persistedFiles[1].mime).isEqualTo("image/jpeg")

        // Verify repository layer correctly aggregates data
        val repositoryPaket = paketRepository.getPaket(paketId)
        assertThat(repositoryPaket).isNotNull()
        assertThat(repositoryPaket!!.files).hasSize(2)
        assertThat(repositoryPaket.fileCount).isEqualTo(2)
        assertThat(repositoryPaket.sizeBytes).isEqualTo(3072) // 1024 + 2048
    }

    @Test
    fun createPaket_withInvalidData_handlesErrorsAcrossLayers() = runTest {
        // Test error propagation through layers
        val invalidMeta = PaketMeta(
            title = "", // Invalid empty title
            description = null,
            tags = emptyList(),
            ttlSeconds = -1, // Invalid negative TTL
            priority = 0
        )

        // Verify error handling flows correctly through:
        // Presentation → Domain → Data
        assertThrows<IllegalArgumentException> {
            createPaketUseCase(invalidMeta, emptyList())
        }
    }
} 