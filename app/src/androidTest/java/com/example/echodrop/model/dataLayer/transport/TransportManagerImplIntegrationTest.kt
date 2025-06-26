package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.transport.TransferProgressCallback
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.SavePaketUseCase
import com.example.echodrop.model.domainLayer.usecase.peer.SavePeerUseCase
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integrationstest für TransportManagerImpl
 */
@RunWith(AndroidJUnit4::class)
class TransportManagerImplIntegrationTest {

    @MockK(relaxed = true)
    private lateinit var wifiDirectService: WiFiDirectService

    @MockK
    private lateinit var wifiDirectDiscovery: WiFiDirectDiscovery

    @MockK(relaxed = true)
    private lateinit var progressCallback: TransferProgressCallback

    @MockK(relaxed = true)
    private lateinit var savePeerUseCase: SavePeerUseCase

    @MockK(relaxed = true)
    private lateinit var transferRepository: TransferRepository

    @MockK(relaxed = true)
    private lateinit var savePaketUseCase: SavePaketUseCase

    @MockK(relaxed = true)
    private lateinit var getPaketDetailUseCase: GetPaketDetailUseCase

    private lateinit var transportManager: TransportManagerImpl
    
    private val receivedManifests = MutableSharedFlow<Pair<PaketId, PeerId>>()
    private val connectionInfoFlow = MutableStateFlow<WifiP2pInfo?>(null)
    private val discoveredDevicesFlow = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    private val thisDeviceFlow = MutableStateFlow<WifiP2pDevice?>(null)
    
    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        every { wifiDirectDiscovery.connectionInfo } returns connectionInfoFlow
        every { wifiDirectDiscovery.discoveredDevices } returns discoveredDevicesFlow as StateFlow<List<WifiP2pDevice>>
        every { wifiDirectDiscovery.thisDevice } returns thisDeviceFlow as StateFlow<WifiP2pDevice?>
        
        coEvery { wifiDirectDiscovery.startDiscovery() } returns Unit
        coEvery { wifiDirectDiscovery.stopDiscovery() } returns Unit
        coEvery { wifiDirectDiscovery.connectToDevice(any()) } returns Unit
        
        transportManager = TransportManagerImpl(
            wifiDirectService = wifiDirectService,
            wifiDirectDiscovery = wifiDirectDiscovery,
            context = ApplicationProvider.getApplicationContext(),
            progressCallback = progressCallback,
            savePeerUseCase = savePeerUseCase,
            transferRepository = transferRepository,
            savePaketUseCase = savePaketUseCase,
            getPaketDetailUseCase = getPaketDetailUseCase,
            _receivedManifests = receivedManifests
        )
    }

    @Test
    fun testStartDiscovery() = runTest {
        transportManager.startDiscovery()
        coVerify(exactly = 1) { wifiDirectDiscovery.startDiscovery() }
    }

    @Test
    fun testStopDiscovery() = runTest {
        transportManager.stopDiscovery()
        coVerify(exactly = 1) { wifiDirectDiscovery.stopDiscovery() }
    }

    @Test
    fun testConnectToDevice() = runTest {
        val deviceId = "test-id"
        transportManager.connectToDevice(deviceId)
        coVerify(exactly = 1) { wifiDirectDiscovery.connectToDevice(deviceId) }
    }
}