package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.p2p.WifiP2pManager
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.platform.wifi.WiFiDirectDiscovery
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WiFiDirectDiscoveryIntegrationTest {

    private lateinit var context: Context
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var wifiDirectDiscovery: WiFiDirectDiscovery

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        wifiP2pManager = mockk(relaxed = true)
        channel = mockk(relaxed = true)

        every { context.getSystemService(Context.WIFI_P2P_SERVICE) } returns wifiP2pManager
        every { wifiP2pManager.initialize(any(), any(), null) } returns channel

        mockkStatic(ContextCompat::class)
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        wifiDirectDiscovery = WiFiDirectDiscovery(context)
    }

    @Test
    fun testStartDiscovery() = runTest {
        wifiDirectDiscovery.startDiscovery()

        verify { wifiP2pManager.discoverPeers(channel, any()) }
    }

    @Test
    fun testStopDiscovery() = runTest {
        wifiDirectDiscovery.stopDiscovery()

        verify { wifiP2pManager.stopPeerDiscovery(channel, any()) }
    }
}