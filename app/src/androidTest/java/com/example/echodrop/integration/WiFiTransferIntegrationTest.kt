package com.example.echodrop.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.platform.wifi.WiFiDirectDiscovery
import com.example.echodrop.model.dataLayer.datasource.platform.wifi.WiFiDirectService
import com.example.echodrop.model.dataLayer.impl.repository.PeerRepositoryImpl
import com.example.echodrop.model.dataLayer.impl.repository.TransferRepositoryImpl
import com.example.echodrop.model.domainLayer.model.DeviceInfo
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferDirection
import com.example.echodrop.model.domainLayer.usecase.network.ConnectToDeviceUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ObserveDiscoveredDevicesUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import com.example.echodrop.model.dataLayer.impl.repository.TransferState

/**
 * Integration test for WiFi discovery and transfer coordination:
 * Domain (UseCases) ↔ Data (Platform Services + Repository Implementation)
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WiFiTransferIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var wifiDiscovery: WiFiDirectDiscovery

    @Inject
    lateinit var wifiService: WiFiDirectService

    @Inject
    lateinit var peerRepository: PeerRepositoryImpl

    @Inject
    lateinit var transferRepository: TransferRepositoryImpl

    private lateinit var observeDevicesUseCase: ObserveDiscoveredDevicesUseCase
    private lateinit var connectToDeviceUseCase: ConnectToDeviceUseCase
    private lateinit var startTransferUseCase: StartTransferUseCase

    @Before
    fun setup() {
        hiltRule.inject()
        // Integration: Wire UseCases with actual implementations
        observeDevicesUseCase = ObserveDiscoveredDevicesUseCase(wifiDiscovery)
        connectToDeviceUseCase = ConnectToDeviceUseCase(wifiService)
        startTransferUseCase = StartTransferUseCase(transferRepository, wifiService)
    }

    @Test
    fun wifiDiscoveryFlow_integrationBetweenDomainAndData() = runTest {
        // Test: Domain UseCases ↔ Data Platform Services

        // Act: Start discovery through Domain layer
        observeDevicesUseCase()

        // Simulate platform-level device discovery
        val discoveredDevice = DeviceInfo(
            deviceName = "TestDevice",
            deviceAddress = "AA:BB:CC:DD:EE:FF"
        )

        // Platform Services simulate discovery
        wifiDiscovery.onDeviceDiscovered(discoveredDevice)

        // Assert: Verify flow through layers
        val devices = observeDevicesUseCase().first()
        assertThat(devices).contains(discoveredDevice)

        // Verify repository layer captures discovery
        val peerId = PeerId(discoveredDevice.deviceAddress)
        val peer = peerRepository.observePeer(peerId).first()
        assertThat(peer).isNotNull()
        assertThat(peer!!.id.value).isEqualTo("AA:BB:CC:DD:EE:FF")
    }

    @Test
    fun transferFlow_coordinationBetweenRepositoriesAndPlatformServices() = runTest {
        // Test: Repository coordination during transfer

        val paketId = PaketId("test-paket")
        val peerId = PeerId("AA:BB:CC:DD:EE:FF")

        // Act: Start transfer through Domain
        startTransferUseCase(paketId, peerId)

        // Assert: Verify Data layer coordination
        // 1. Transfer repository creates transfer record
        val transfer = transferRepository.observeTransfers().first()
            .find { it.paketId == paketId && it.peerId == peerId }
        assertThat(transfer).isNotNull()
        assertThat(transfer!!.direction).isEqualTo(TransferDirection.OUTGOING)

        // 2. Platform services initiate actual transfer
        // (This would be mocked in real integration test)
        verify(wifiService).sendPaket(paketId, peerId)

        // 3. Repository updates progress as platform reports
        transferRepository.updateProgress(paketId, peerId, 50)
        val updatedTransfer = transferRepository.observeTransfers().first()
            .find { it.paketId == paketId && it.peerId == peerId }
        assertThat(updatedTransfer!!.progressPct).isEqualTo(50)
    }

    @Test
    fun errorHandling_acrossDataLayerComponents() = runTest {
        // Test: Error propagation between Data layer components

        val paketId = PaketId("invalid-paket")
        val peerId = PeerId("unreachable-device")

        // Simulate platform service failure
        doThrow(RuntimeException("WiFi connection failed"))
            .`when`(wifiService).sendPaket(paketId, peerId)

        // Act & Assert: Verify error handling flows correctly
        assertThrows<RuntimeException> {
            startTransferUseCase(paketId, peerId)
        }

        // Verify repository correctly handles failure state
        val failedTransfer = transferRepository.observeTransfers().first()
            .find { it.paketId == paketId && it.peerId == peerId }
        assertThat(failedTransfer?.state).isEqualTo(TransferState.FAILED)
    }
} 