package com.example.echodrop.model.dataLayer.transport

import android.net.wifi.p2p.WifiP2pDevice
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.impl.transport.ForwarderImpl
import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.repository.ConnectionAttemptRepository
import com.example.echodrop.model.domainLayer.transport.DeviceDiscovery
import com.example.echodrop.model.domainLayer.transport.ForwardEvent
import com.example.echodrop.model.domainLayer.transport.TransportManager
import com.example.echodrop.model.domainLayer.usecase.paket.SavePaketUseCase
import dagger.Lazy
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForwarderImplTest {

    private lateinit var forwarder: ForwarderImpl
    private lateinit var discovery: FakeDiscovery
    private val events = MutableSharedFlow<ForwardEvent>(extraBufferCapacity = 8)

    @Before
    fun setUp() {
        discovery = FakeDiscovery()
        val savePaket = mockk<SavePaketUseCase>(relaxed = true)
        val connRepo = mockk<ConnectionAttemptRepository>(relaxed = true)
        coEvery { connRepo.getFailedAttemptCount(any(), any(), any()) } returns 0
        val tm = mockk<TransportManager>(relaxed = true)
        val lazyTm = object : Lazy<TransportManager> { override fun get() = tm }

        forwarder = ForwarderImpl(discovery, savePaket, connRepo, lazyTm, events)
    }

    @Test
    fun autoForward_withoutPeers_emitsNoEvents_andStopsDiscovery() = runTest {
        val paket = Paket(
            id = PaketId("p1"),
            meta = PaketMeta("t", null, emptyList(), 3600, 1, null),
            sizeBytes = 0,
            fileCount = 0,
            createdUtc = System.currentTimeMillis(),
            files = emptyList()
        )

        forwarder.autoForward(paket, null)

        assertTrue(events.replayCache.isEmpty())
        assertTrue(discovery.startCalled)
        assertTrue(discovery.stopCalled)
    }

    /** Minimal-Fake für DeviceDiscovery */
    private class FakeDiscovery : DeviceDiscovery {
        override val discoveredDevices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
        override val connectionInfo = MutableStateFlow<android.net.wifi.p2p.WifiP2pInfo?>(null)
        override val thisDevice = MutableStateFlow<WifiP2pDevice?>(null)

        var startCalled = false
        var stopCalled = false

        override fun startDiscovery() { startCalled = true }
        override fun stopDiscovery() { stopCalled = true }
        override fun connectToDevice(deviceAddress: String) { /* no-op */ }
        override fun disconnectFromCurrentGroup() { /* no-op */ }
    }
} 