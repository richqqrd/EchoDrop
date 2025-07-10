package com.example.echodrop.viewmodel

import android.app.Application
import com.example.echodrop.model.domainLayer.usecase.file.InsertFilesUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.CreatePaketUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("CreatePaketViewModel Tests")
class CreatePaketViewModelTest {

    private lateinit var viewModel: CreatePaketViewModel
    private lateinit var mockCreatePaket: CreatePaketUseCase
    private lateinit var mockInsertFiles: InsertFilesUseCase
    private lateinit var mockApplication: Application
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Create mocks
        mockCreatePaket = mock(CreatePaketUseCase::class.java)
        mockInsertFiles = mock(InsertFilesUseCase::class.java)
        mockApplication = mock(Application::class.java)
        
        viewModel = CreatePaketViewModel(mockApplication, mockCreatePaket, mockInsertFiles)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("ViewModel can be created successfully")
    fun viewModelCanBeCreatedSuccessfully() {
        // Assert
        assertNotNull(viewModel)
    }

    @Test
    @DisplayName("Initial state is not null")
    fun initialStateIsNotNull() {
        // Assert
        assertNotNull(viewModel.state)
    }
} 