package com.example.echodrop.view

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.example.echodrop.viewmodel.TransferManagerViewModel
import com.example.echodrop.viewmodel.TransferUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class TransferManagerScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var fakeVm: TransferManagerViewModel

    @Before
    fun setup() {
        fakeVm = mockk(relaxed = true)
        val uiState = TransferUiState(
            isWifiDirectEnabled = true,
            isDiscoveryActive = false,
            discoveredDevices = emptyList()
        )
        every { fakeVm.uiState } returns MutableStateFlow(uiState)
    }

    @Test
    fun transferManagerScreen_displaysHeaderAndDeviceCount() {
        composeRule.setContent {
            TransferManagerScreen(
                onBackClick = {},
                viewModel = fakeVm
            )
        }

        composeRule.onNodeWithText("Transfer-Manager").assertIsDisplayed()
        composeRule.onNodeWithText("Verfügbare Geräte").assertIsDisplayed()
        composeRule.onNodeWithText("0").assertIsDisplayed()
    }
} 