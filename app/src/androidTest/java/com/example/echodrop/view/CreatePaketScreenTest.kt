package com.example.echodrop.view

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.echodrop.viewmodel.CreatePaketViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.viewmodel.CreatePaketViewModel.CreatePaketState

@RunWith(AndroidJUnit4::class)
class CreatePaketScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var fakeVm: CreatePaketViewModel

    @Before
    fun setup() {
        fakeVm = mockk(relaxed = true)
        val stateFlow = MutableStateFlow(
            CreatePaketState(
                title = "",
                description = null,
                tags = emptyList(),
                ttl = 3600,
                priority = 1,
                maxHops = 3,
                uris = listOf(Uri.parse("file://dummy.txt")), // at least one file to enable Save button
                saved = false,
                error = null
            )
        )
        every { fakeVm.state } returns stateFlow
    }

    @Test
    fun createPaketScreen_showsBasicInputs() {
        composeRule.setContent {
            CreatePaketScreen(
                viewModel = fakeVm,
                onBackClick = {},
                onSaveSuccess = {}
            )
        }

        // Title label
        composeRule.onNodeWithText("Titel").assertIsDisplayed()
        // Button for adding files
        composeRule.onNodeWithText("Dateien hinzufügen").assertIsDisplayed()
    }
} 