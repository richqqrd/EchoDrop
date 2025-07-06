package com.example.echodrop.viewmodel

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.DeviceInfo
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferId
import com.example.echodrop.model.domainLayer.usecase.network.ConnectToDeviceUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ObserveConnectionStateUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ObserveDiscoveredDevicesUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ObserveThisDeviceUseCase
import com.example.echodrop.model.domainLayer.usecase.network.StartBeaconingUseCase
import com.example.echodrop.model.domainLayer.usecase.network.StopBeaconingUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.model.TransferDirection
import com.example.echodrop.model.domainLayer.usecase.paket.ObserveInboxUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase

data class TransferUiState(
    val isWifiDirectEnabled: Boolean = false,
    val isDiscoveryActive: Boolean = false,
    val isDebugMode: Boolean = false,
    val thisDevice: WifiP2pDevice? = null,
    val discoveredDevices: List<WifiP2pDevice> = emptyList(),
    val connectedDevices: Set<String> = emptySet(),
    val sendingTransfers: List<TransferItem> = emptyList(),
    val receivingTransfers: List<TransferItem> = emptyList(),
    val completedTransfers: List<TransferItem> = emptyList()
)

data class TransferItem(
    val id: String,
    val paketId: PaketId,
    val peerId: PeerId,
    val progress: Float,
    val isPaused: Boolean = false
)

@HiltViewModel
class TransferManagerViewModel @Inject constructor(
    // Network Use Cases
    private val startBeaconingUseCase: StartBeaconingUseCase,
    private val stopBeaconingUseCase: StopBeaconingUseCase,
    private val observeDiscoveredDevicesUseCase: ObserveDiscoveredDevicesUseCase,
    private val observeThisDeviceUseCase: ObserveThisDeviceUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,

    // Transfer Use Cases
    private val observeTransfersUseCase: ObserveTransfersUseCase,
    private val pauseTransferUseCase: PauseTransferUseCase,
    private val resumeTransferUseCase: ResumeTransferUseCase,
    private val cancelTransferUseCase: CancelTransferUseCase,

    // Paket Use Cases
    private val observeInboxUseCase: ObserveInboxUseCase,
    private val startTransferUseCase: StartTransferUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransferUiState())

    // Kombiniere alle Datenströme in einen einzigen UI-State
    val uiState: StateFlow<TransferUiState> = combine(
        _state,
        observeDiscoveredDevicesUseCase(),
        observeThisDeviceUseCase(),
        observeConnectionStateUseCase()
    ) { state, devices, thisDevice, connectionInfo ->
        state.copy(
            discoveredDevices = devices.map { it.toWifiP2pDevice() },
            thisDevice = thisDevice?.toWifiP2pDevice(),
            isWifiDirectEnabled = thisDevice != null,
            connectedDevices = connectionInfo.connectedDevices
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransferUiState()
    )

    companion object {
        // Blacklist von Geräte-Adressen (MAC-Adressen)
        private val DEVICE_BLACKLIST = setOf(
            "f6:30:b9:4a:18:9d",  
            "f6:30:b9:51:fe:4b",
            "a6:d7:3c:00:e8:ec"
        )
    }

    private var discoveryJob: Job? = null
    private var testTransferJob: Job? = null
    private var autoConnectJob: Job? = null


    init {
        viewModelScope.launch {
            observeTransfersUseCase().collect { transfers ->
                val sending = transfers.filter {
                    // Berücksichtige auch QUEUED-Transfers
                    (it.state == TransferState.ACTIVE || it.state == TransferState.QUEUED) &&
                            it.direction == TransferDirection.OUTGOING
                }
                    .map {
                        TransferItem(
                            id = it.id.value,
                            paketId = it.paketId,
                            peerId = it.peerId,
                            progress = it.progressPct / 100f,
                            isPaused = it.state == TransferState.PAUSED
                        )
                    }


                val receiving = transfers.filter {
                    it.state == TransferState.ACTIVE &&
                            it.direction == TransferDirection.INCOMING
                }
                    .map {
                        TransferItem(
                            id = it.id.value,
                            paketId = it.paketId,
                            peerId = it.peerId,
                            progress = it.progressPct / 100f,
                            isPaused = it.state == TransferState.PAUSED
                        )
                    }

                val completed = transfers.filter {
                    it.state == TransferState.DONE
                }
                    .map {
                        TransferItem(
                            id = it.id.value,
                            paketId = it.paketId,
                            peerId = it.peerId,
                            progress = 1.0f
                        )
                    }

                _state.update {
                    it.copy(
                        sendingTransfers = sending,
                        receivingTransfers = receiving,
                        completedTransfers = completed
                    )
                }
            }
        }
    }

    fun startDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            startBeaconingUseCase()
            _state.update { it.copy(isDiscoveryActive = true) }
        }
    }

    fun stopDiscovery() {
        discoveryJob?.cancel()
        viewModelScope.launch {
            stopBeaconingUseCase()
            _state.update { it.copy(isDiscoveryActive = false) }
        }
    }

    fun toggleDiscovery() {
        if (_state.value.isDiscoveryActive) {
            stopDiscovery()
        } else {
            startDiscovery()
        }
    }

    fun toggleDebugMode() {
        _state.update { it.copy(isDebugMode = !it.isDebugMode) }
    }

    fun pauseTransfer(transferId: String) {
        val transfer = findTransferById(transferId) ?: return

        viewModelScope.launch {
            pauseTransferUseCase(transfer.paketId, transfer.peerId)
        }
    }

    fun resumeTransfer(transferId: String) {
        val transfer = findTransferById(transferId) ?: return

        viewModelScope.launch {
            resumeTransferUseCase(transfer.paketId, transfer.peerId)
        }
    }

    fun cancelTransfer(transferId: String) {
        val transfer = findTransferById(transferId) ?: return

        viewModelScope.launch {
            cancelTransferUseCase(transfer.paketId, transfer.peerId)
        }
    }

    private fun findTransferById(id: String): TransferItem? {
        return _state.value.sendingTransfers.find { it.id == id }
            ?: _state.value.receivingTransfers.find { it.id == id }
    }

    // Debug-Methode für Tests
    fun generateTestTransfer(isSending: Boolean) {
        testTransferJob?.cancel()

        testTransferJob = viewModelScope.launch {
            val id = "test-${Random.nextInt(1000)}"
            val paketId = PaketId("pkg-${Random.nextInt(100)}")
            val peerId = PeerId("peer-${Random.nextInt(100)}")
            val transferItem = TransferItem(id, paketId, peerId, 0.0f)

            if (isSending) {
                _state.update { it.copy(
                    sendingTransfers = it.sendingTransfers + transferItem
                ) }

                // Simuliere Fortschritt
                var progress = 0.0f
                while (progress < 1.0f) {
                    delay(500)
                    progress += 0.05f
                    progress = minOf(progress, 1.0f)

                    _state.update { state ->
                        val updatedTransfers = state.sendingTransfers.map {
                            if (it.id == id) it.copy(progress = progress) else it
                        }

                        state.copy(sendingTransfers = updatedTransfers)
                    }
                }

                // Bei Abschluss zur Completed-Liste hinzufügen
                _state.update { state ->
                    state.copy(
                        sendingTransfers = state.sendingTransfers.filter { it.id != id },
                        completedTransfers = state.completedTransfers + TransferItem(id, paketId, peerId, 1.0f)
                    )
                }
            } else {
                _state.update { it.copy(
                    receivingTransfers = it.receivingTransfers + transferItem
                ) }

                // Simuliere Fortschritt
                var progress = 0.0f
                while (progress < 1.0f) {
                    delay(300)
                    progress += 0.03f
                    progress = minOf(progress, 1.0f)

                    _state.update { state ->
                        val updatedTransfers = state.receivingTransfers.map {
                            if (it.id == id) it.copy(progress = progress) else it
                        }

                        state.copy(receivingTransfers = updatedTransfers)
                    }
                }

                // Bei Abschluss zur Completed-Liste hinzufügen
                _state.update { state ->
                    state.copy(
                        receivingTransfers = state.receivingTransfers.filter { it.id != id },
                        completedTransfers = state.completedTransfers + TransferItem(id, paketId, peerId, 1.0f)
                    )
                }
            }
        }
    }

    // Hilfsmethoden zur Umwandlung von Domain-Modellen zu UI-Modellen
    private fun DeviceInfo.toWifiP2pDevice(): WifiP2pDevice {
        return WifiP2pDevice().apply {
            deviceName = this@toWifiP2pDevice.deviceName
            deviceAddress = this@toWifiP2pDevice.deviceAddress
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
        autoConnectJob?.cancel()
    }
}
