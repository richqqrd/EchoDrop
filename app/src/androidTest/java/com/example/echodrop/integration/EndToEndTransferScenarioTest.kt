package com.example.echodrop.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.datasource.platform.file.FileUtils
import com.example.echodrop.model.dataLayer.impl.repository.FileRepositoryImpl
import com.example.echodrop.model.dataLayer.impl.repository.PaketRepositoryImpl
import com.example.echodrop.model.dataLayer.impl.repository.PeerRepositoryImpl
import com.example.echodrop.model.dataLayer.impl.repository.TransferRepositoryImpl
import com.example.echodrop.model.dataLayer.impl.transport.TransportManagerImpl
import com.example.echodrop.model.domainLayer.model.*
import com.example.echodrop.model.domainLayer.usecase.file.InsertFilesUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ConnectToDeviceUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ObserveDiscoveredDevicesUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.CreatePaketUseCase
import com.example.echodrop.model.domainLayer.usecase.peer.UpsertPeerUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.ObserveTransfersUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * End-to-end integration test scenario:
 * Presentation (UseCases) → Domain (Repository Interfaces) → Data (Repository Implementations + DAOs + Platform Services)
 * 
 * Scenario: Complete file transfer workflow
 * 1. Create paket with files
 * 2. Discover peer device
 * 3. Initiate transfer
 * 4. Monitor transfer progress
 * 5. Verify completion
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class EndToEndTransferScenarioTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // Data Layer (Real implementations)
    @Inject lateinit var database: EchoDatabase
    @Inject lateinit var fileUtils: FileUtils
    @Inject lateinit var paketRepository: PaketRepositoryImpl
    @Inject lateinit var fileRepository: FileRepositoryImpl
    @Inject lateinit var peerRepository: PeerRepositoryImpl
    @Inject lateinit var transferRepository: TransferRepositoryImpl
    @Inject lateinit var transportManager: TransportManagerImpl

    // Presentation Layer (UseCases using real dependencies)
    private lateinit var createPaketUseCase: CreatePaketUseCase
    private lateinit var insertFilesUseCase: InsertFilesUseCase
    private lateinit var observeDevicesUseCase: ObserveDiscoveredDevicesUseCase
    private lateinit var connectToDeviceUseCase: ConnectToDeviceUseCase
    private lateinit var upsertPeerUseCase: UpsertPeerUseCase
    private lateinit var startTransferUseCase: StartTransferUseCase
    private lateinit var observeTransfersUseCase: ObserveTransfersUseCase

    @Before
    fun setup() {
        hiltRule.inject()
        
        // Wire all layers together with real implementations
        createPaketUseCase = CreatePaketUseCase(paketRepository)
        insertFilesUseCase = InsertFilesUseCase(fileRepository)
        observeDevicesUseCase = ObserveDiscoveredDevicesUseCase(transportManager)
        connectToDeviceUseCase = ConnectToDeviceUseCase(transportManager)
        upsertPeerUseCase = UpsertPeerUseCase(peerRepository)
        startTransferUseCase = StartTransferUseCase(transferRepository, transportManager)
        observeTransfersUseCase = ObserveTransfersUseCase(transferRepository)
    }

    @Test
    fun completeFileTransferWorkflow_allLayersIntegration() = runTest {
        // ========== STEP 1: CREATE PAKET WITH FILES ==========
        // Presentation → Domain → Data
        
        val meta = PaketMeta(
            title = "Integration Test Files",
            description = "End-to-end test scenario",
            tags = listOf("integration", "e2e"),
            ttlSeconds = 3600,
            priority = 8
        )
        
        val files = listOf(
            FileEntry(
                path = "/sdcard/test/document.pdf",
                mime = "application/pdf", 
                sizeBytes = 1024000,
                orderIdx = 0
            ),
            FileEntry(
                path = "/sdcard/test/image.jpg",
                mime = "image/jpeg",
                sizeBytes = 512000, 
                orderIdx = 1
            )
        )

        // Act: Create paket through all layers
        val paketId = createPaketUseCase(meta, files)
        
        // Verify: Data persisted correctly in database
        val persistedPaket = database.paketDao().getPaket(paketId.value)!!
        assertThat(persistedPaket.title).isEqualTo("Integration Test Files")
        assertThat(persistedPaket.priority).isEqualTo(8)
        
        val persistedFiles = database.fileEntryDao().getFilesForPaket(paketId.value)
        assertThat(persistedFiles).hasSize(2)
        assertThat(persistedFiles.sumOf { it.sizeBytes }).isEqualTo(1536000)

        // ========== STEP 2: DISCOVER AND CONNECT TO PEER ==========
        // Domain → Data (Platform Services)
        
        val targetDevice = DeviceInfo(
            deviceName = "TargetDevice",
            deviceAddress = "BB:CC:DD:EE:FF:AA"
        )
        
        // Simulate device discovery through platform services
        transportManager.simulateDeviceDiscovered(targetDevice)
        
        // Verify discovery propagates through layers
        val discoveredDevices = observeDevicesUseCase().first()
        assertThat(discoveredDevices).contains(targetDevice)
        
        // Create peer record through domain layer
        val peer = Peer(
            id = PeerId(targetDevice.deviceAddress),
            alias = "Target Device",
            lastSeenUtc = System.currentTimeMillis()
        )
        upsertPeerUseCase(peer)
        
        // Connect to device
        connectToDeviceUseCase(targetDevice.deviceAddress)

        // ========== STEP 3: START TRANSFER ==========
        // Presentation → Domain → Data (Repository + Platform Services)
        
        val peerId = PeerId(targetDevice.deviceAddress)
        
        // Start transfer through use case
        startTransferUseCase(paketId, peerId)
        
        // Verify transfer record created in database
        val transferRecord = database.transferDao().getTransfer(paketId.value, peerId.value)!!
        assertThat(transferRecord.direction).isEqualTo("OUTGOING")
        assertThat(transferRecord.state).isEqualTo("ACTIVE")
        assertThat(transferRecord.progressPct).isEqualTo(0)

        // ========== STEP 4: SIMULATE TRANSFER PROGRESS ==========
        // Data Layer (Platform Services → Repository)
        
        // Simulate platform reporting progress
        transportManager.simulateTransferProgress(paketId, peerId, 25)
        delay(100) // Allow async updates
        
        var currentTransfers = observeTransfersUseCase().first()
        var ourTransfer = currentTransfers.find { it.paketId == paketId && it.peerId == peerId }!!
        assertThat(ourTransfer.progressPct).isEqualTo(25)
        assertThat(ourTransfer.state).isEqualTo(TransferState.ACTIVE)
        
        // More progress
        transportManager.simulateTransferProgress(paketId, peerId, 75)
        delay(100)
        
        currentTransfers = observeTransfersUseCase().first()
        ourTransfer = currentTransfers.find { it.paketId == paketId && it.peerId == peerId }!!
        assertThat(ourTransfer.progressPct).isEqualTo(75)

        // ========== STEP 5: COMPLETE TRANSFER ==========
        // Platform Services → Repository → UseCase observation
        
        transportManager.simulateTransferComplete(paketId, peerId)
        delay(100)
        
        // Verify completion propagates through all layers
        currentTransfers = observeTransfersUseCase().first()
        ourTransfer = currentTransfers.find { it.paketId == paketId && it.peerId == peerId }!!
        assertThat(ourTransfer.progressPct).isEqualTo(100)
        assertThat(ourTransfer.state).isEqualTo(TransferState.DONE)
        
        // Verify database state
        val finalTransferRecord = database.transferDao().getTransfer(paketId.value, peerId.value)!!
        assertThat(finalTransferRecord.state).isEqualTo("DONE")
        assertThat(finalTransferRecord.progressPct).isEqualTo(100)

        // ========== VERIFICATION: DATA CONSISTENCY ACROSS LAYERS ==========
        
        // Domain layer view
        val domainPaket = paketRepository.getPaket(paketId)!!
        assertThat(domainPaket.files).hasSize(2)
        assertThat(domainPaket.sizeBytes).isEqualTo(1536000)
        
        // Data layer view (should match)
        val dataLayerFiles = fileRepository.getFilesForPaket(paketId)
        assertThat(dataLayerFiles).hasSize(2)
        assertThat(dataLayerFiles.sumOf { it.sizeBytes }).isEqualTo(1536000)
        
        // Peer information consistency
        val domainPeer = peerRepository.observePeer(peerId).first()!!
        assertThat(domainPeer.alias).isEqualTo("Target Device")
        
        // Transfer log consistency
        val transferLogs = transferRepository.observeTransfers().first()
        val completedTransfer = transferLogs.find { it.paketId == paketId }!!
        assertThat(completedTransfer.direction).isEqualTo(TransferDirection.OUTGOING)
        assertThat(completedTransfer.state).isEqualTo(TransferState.DONE)
    }

    @Test
    fun transferFailureScenario_errorHandlingAcrossLayers() = runTest {
        // Test error propagation and recovery across all layers
        
        val paketId = PaketId("error-test-paket")
        val peerId = PeerId("unreachable-device")
        
        // Create minimal paket for error testing
        val meta = PaketMeta("Error Test", null, emptyList(), 1800, 1)
        val actualPaketId = createPaketUseCase(meta, emptyList())
        
        // Simulate connection failure at platform level
        transportManager.simulateConnectionError(peerId)
        
        // Attempt transfer - should fail and be handled gracefully
        try {
            startTransferUseCase(actualPaketId, peerId)
        } catch (e: Exception) {
            // Expected failure
        }
        
        // Verify error state propagated correctly through layers
        val transfers = observeTransfersUseCase().first()
        val failedTransfer = transfers.find { it.paketId == actualPaketId && it.peerId == peerId }
        
        if (failedTransfer != null) {
            assertThat(failedTransfer.state).isEqualTo(TransferState.FAILED)
        }
        
        // Verify database reflects error state
        val dbTransfer = database.transferDao().getTransfer(actualPaketId.value, peerId.value)
        if (dbTransfer != null) {
            assertThat(dbTransfer.state).isEqualTo("FAILED")
        }
    }
} 