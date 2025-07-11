package com.example.echodrop.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.intent.Intents
import com.example.echodrop.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.example.echodrop.model.dataLayer.datasource.persistence.DatabaseModule
import com.example.echodrop.di.TransportBindingsModule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import androidx.test.rule.GrantPermissionRule
import android.Manifest
import dagger.hilt.android.testing.BindValue
import com.example.echodrop.model.dataLayer.datasource.platform.file.PermissionManager
import io.mockk.every
import io.mockk.mockk

@HiltAndroidTest
@UninstallModules(DatabaseModule::class)
@RunWith(AndroidJUnit4::class)
class EchoDropEndToEndUiTest {

    @BindValue
    @JvmField
    val stubPermissionManager: PermissionManager = mockk(relaxed = true) {
        every { getRequiredPermissions() } returns emptyArray()
        every { arePermissionsGranted(any()) } returns true
        every { getMissingPermissions(any()) } returns emptyList()
    }
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.NEARBY_WIFI_DEVICES,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
        Intents.init()
        FilePickerStub.register()
    }

    @After
    fun tearDown() { Intents.release() }

    @Test
    fun createPaket_openDetail_then_transferManager() {
        composeRule.onNodeWithText("EchoDrop").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Create Paket").performClick()

        composeRule.onNodeWithText("Neues Paket erstellen").assertIsDisplayed()


        composeRule.onNodeWithTag("titleField").performTextInput("E2E-Paket")

        composeRule.onNodeWithTag("addFilesButton").performClick()

        composeRule.onNodeWithContentDescription("Speichern").performClick()

        composeRule.onNodeWithText("E2E-Paket").assertIsDisplayed().performClick()

        composeRule.onNodeWithText("Paket-Details").assertIsDisplayed()

        composeRule.onNode(hasContentDescription("Transfer Manager")).performClick()

        composeRule.onNodeWithText("Transfer-Manager").assertIsDisplayed()

        composeRule.onNode(hasContentDescription("Zurück")).performClick()

        composeRule.onNodeWithText("EchoDrop").assertIsDisplayed()
    }
} 