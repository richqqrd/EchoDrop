@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.echodrop.viewmodel

import android.app.Application
import android.net.Uri
import com.example.echodrop.model.domainLayer.usecase.file.InsertFilesUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.CreatePaketUseCase
import com.example.echodrop.model.dataLayer.datasource.platform.file.FileUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi

class CreatePaketViewModelTest {

    private lateinit var vm: CreatePaketViewModel
    private val createPaket: CreatePaketUseCase = mockk(relaxed = true)
    private val insertFiles: InsertFilesUseCase = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val contentResolver = mockk<android.content.ContentResolver>(relaxed = true)
        every { application.contentResolver } returns contentResolver
        // Return a dummy stream for any URI
        every { contentResolver.openInputStream(any()) } returns java.io.ByteArrayInputStream(ByteArray(0))

        // Mock FileUtils to bypass actual file operations
        mockkObject(FileUtils)
        every { FileUtils.copyUriToAppFile(any(), any(), any()) } returns "/tmp/dummy"

        vm = CreatePaketViewModel(application, createPaket, insertFiles)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setTitle updates state`() = runTest {
        vm.setTitle("Hello")
        assertEquals("Hello", vm.state.first().title)
    }

    @Test
    fun `setTags splits csv string`() = runTest {
        vm.setTags("a, b ,c")
        assertEquals(listOf("a","b","c"), vm.state.first().tags)
    }
} 