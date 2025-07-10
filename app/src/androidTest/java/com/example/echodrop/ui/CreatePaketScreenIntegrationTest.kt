package com.example.echodrop.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.domainLayer.repository.FileRepository
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import com.example.echodrop.model.domainLayer.usecase.file.InsertFilesUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.CreatePaketUseCase
import com.example.echodrop.view.CreatePaketScreen
import com.example.echodrop.viewmodel.CreatePaketViewModel
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreatePaketScreenIntegrationTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var vm: CreatePaketViewModel

    @Before
    fun setUp() {
        /* ---------- Repos & Use-Cases ---------- */
        val paketRepo: PaketRepository = mockk(relaxed = true)
        val fileRepo : FileRepository  = mockk(relaxed = true)

        val createPaketUseCase = CreatePaketUseCase(paketRepo)
        val insertFilesUseCase = InsertFilesUseCase(fileRepo)

        /* ---------- ViewModel ---------- */
        val app: Application = ApplicationProvider.getApplicationContext()
        vm = CreatePaketViewModel(app, createPaketUseCase, insertFilesUseCase)
    }

    @Test
    fun titleBarIsVisible() {
        composeRule.setContent {
            CreatePaketScreen(
                viewModel = vm,
                onBackClick   = {},
                onSaveSuccess = {}
            )
        }

        composeRule.onNodeWithText("Neues Paket erstellen").assertIsDisplayed()
    }
} 