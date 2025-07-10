package com.example.echodrop.model.dataLayer.transport

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.platform.wifi.WiFiDirectDiscovery
import com.example.echodrop.model.dataLayer.datasource.platform.wifi.WiFiDirectService
import com.example.echodrop.model.dataLayer.impl.transport.TransportManagerImpl
import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.transport.*
import com.example.echodrop.model.domainLayer.repository.*
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
 * Integrationstest zwischen `TransportManagerImpl` und WiFi-Direkt-Schicht
 * (`WiFiDirectDiscovery`, `WiFiDirectService`).
 */
@RunWith(AndroidJUnit4::class)
class TransportManagerWiFiDirectIntegrationTest {

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
    private lateinit var connectionAttemptRepository: ConnectionAttemptRepository

    @MockK(relaxed = true)
    private lateinit var peerRepository: PeerRepository

    @MockK(relaxed = true)
    private lateinit var savePaketUseCase: SavePaketUseCase

    @MockK(relaxed = true)
    private lateinit var getPaketDetailUseCase: GetPaketDetailUseCase

    @MockK(relaxed = true)
    private lateinit var manifestBuilder: ManifestBuilder

    @MockK(relaxed = true)
    private lateinit var manifestParser: ManifestParser

    @MockK(relaxed = true)
    private lateinit var chunkIO: ChunkIO

    @MockK(relaxed = true)
    private lateinit var forwarder: Forwarder

    @MockK(relaxed = true)
    private lateinit var maintenanceScheduler: MaintenanceScheduler

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
            transferRepository = transferRepository,
            connectionAttemptRepository = connectionAttemptRepository,
            peerRepository = peerRepository,
            context = ApplicationProvider.getApplicationContext(),
            savePaketUseCase = savePaketUseCase,
            getPaketDetailUseCase = getPaketDetailUseCase,
            progressCallback = progressCallback,
            manifestBuilder = manifestBuilder,
            manifestParser = manifestParser,
            _receivedManifests = receivedManifests,
            chunkIO = chunkIO,
            forwarder = forwarder,
            maintenanceScheduler = maintenanceScheduler
        )
    }

    @Test
    fun startDiscovery_triggersDiscoveryService() = runTest {
        transportManager.startDiscovery()
        coVerify(exactly = 1) { wifiDirectDiscovery.startDiscovery() }
    }

    @Test
    fun stopDiscovery_stopsDiscoveryService() = runTest {
        transportManager.stopDiscovery()
        coVerify(exactly = 1) { wifiDirectDiscovery.stopDiscovery() }
    }

    @Test
    fun connectToDevice_forwardsCallToDiscoveryService() = runTest {
        val deviceId = "test-id"
        transportManager.connectToDevice(deviceId)
        coVerify(exactly = 1) { wifiDirectDiscovery.connectToDevice(deviceId) }
    }
} 