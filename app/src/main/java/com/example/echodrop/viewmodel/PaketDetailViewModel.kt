package com.example.echodrop.viewmodel

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echodrop.model.domainLayer.model.DeviceInfo
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.usecase.file.GetFilesForPaketUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ConnectToDeviceUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ObserveConnectionStateUseCase
import com.example.echodrop.model.domainLayer.usecase.network.ObserveDiscoveredDevicesUseCase
import com.example.echodrop.model.domainLayer.usecase.network.StartBeaconingUseCase
import com.example.echodrop.model.domainLayer.usecase.network.StopBeaconingUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.DeletePaketUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.UpdatePaketMetaUseCase
import com.example.echodrop.model.domainLayer.usecase.peer.SavePeerUseCase
import com.example.echodrop.model.domainLayer.usecase.transfer.StartTransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.withTimeoutOrNull

data class PaketDetailState(
    val paket: PaketUi? = null,
    val isLoading: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null,
    val isEditing: Boolean = false,
    val message: String? = null,
    val navigateToManager: Boolean = false
)

private const val TAG = "PaketDetailViewModel"

@HiltViewModel
class PaketDetailViewModel @Inject constructor(
    private val getPaketDetail: GetPaketDetailUseCase,
    private val getFilesForPaket: GetFilesForPaketUseCase,
    private val updatePaketMeta: UpdatePaketMetaUseCase,
    private val deletePaket: DeletePaketUseCase,
    private val startTransfer: StartTransferUseCase,
    private val startBeaconingUseCase: StartBeaconingUseCase,
    private val stopBeaconingUseCase: StopBeaconingUseCase,
    private val observeDiscoveredDevicesUseCase: ObserveDiscoveredDevicesUseCase,
    private val connectToDeviceUseCase: ConnectToDeviceUseCase,
    private val savePeerUseCase: SavePeerUseCase,
    private val observeConnectionStateUseCase: ObserveConnectionStateUseCase
) : ViewModel() {

    companion object {
        private val DEVICE_BLACKLIST = setOf(
            "f6:30:b9:4a:18:9d", 
            "f6:30:b9:51:fe:4b",
            "a6:d7:3c:00:e8:ec",
            "0a:2e:5f:f1:00:b0"
        )
    }

    private val _state = MutableStateFlow(PaketDetailState(isLoading = true))
    val state: StateFlow<PaketDetailState> = _state

    private val _nearbyDevices = MutableStateFlow<List<WifiP2pDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<WifiP2pDevice>> = _nearbyDevices.asStateFlow()

    private val _isDiscoveryActive = MutableStateFlow(false)
    val isDiscoveryActive: StateFlow<Boolean> = _isDiscoveryActive.asStateFlow()

    init {
        // Beobachte gefundene Geräte
        viewModelScope.launch {
            observeDiscoveredDevicesUseCase().collect { devices ->
                _nearbyDevices.value = devices.map { it.toWifiP2pDevice() }
                Log.d(TAG, "Discovered devices updated: ${_nearbyDevices.value.size}")
            }
        }
    }

    fun loadPaketDetail(paketId: String) {
        val id = PaketId(paketId)
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val paket = getPaketDetail(id)
                if (paket != null) {
                    // Lade die Dateien für das Paket
                    val files = getFilesForPaket(id)

                    // Wandle das Paket in ein UI-Modell um und aktualisiere den State
                    _state.update {
                        it.copy(
                            paket = paket.toDetailUi(files.map { file -> file.toUi() }),
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            error = "Paket nicht gefunden",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Fehler beim Laden des Pakets: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updatePaketSettings(ttlSeconds: Int, priority: Int) {
        val currentPaket = state.value.paket ?: return
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Rufe den UseCase auf, um die Paket-Metadaten zu aktualisieren
                updatePaketMeta(currentPaket.id, ttlSeconds, priority)

                // Lade das Paket neu, um die aktualisierten Werte zu zeigen
                loadPaketDetail(currentPaket.id.value)

                // Deaktiviere den Bearbeitungsmodus
                _state.update { it.copy(isEditing = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Fehler beim Aktualisieren des Pakets: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onDeletePaket() {
        val currentPaket = state.value.paket ?: return
        _state.update { it.copy(isDeleting = true) }

        viewModelScope.launch {
            try {
                // Rufe den UseCase auf, um das Paket zu löschen
                deletePaket(currentPaket.id)
                _state.update { it.copy(isDeleting = false) }
                // Nach dem Löschen wird in der UI zur vorherigen Ansicht zurücknavigiert
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Fehler beim Löschen des Pakets: ${e.message}",
                        isDeleting = false
                    )
                }
            }
        }
    }

    fun onSharePaket(peerId: PeerId) {
        val currentPaket = state.value.paket ?: return
        if (currentPaket.maxHops != null && currentPaket.currentHopCount >= currentPaket.maxHops) {
            _state.update { it.copy(error = "Maximale Weiterleitungen erreicht") }
            return
        }

        viewModelScope.launch {
            try {
                // NEU: Erstelle einen Peer-Eintrag für diese PeerId
                val peer = Peer(
                    id = peerId,
                    alias = "Manuell eingegebener Peer",
                    lastSeenUtc = System.currentTimeMillis()
                )

                // Speichere den Peer in der Datenbank
                savePeerUseCase(peer)

                // Rest wie bisher
                startTransfer(currentPaket.id, peerId)
                // Paket neu laden, um aktualisierten Hop-Count anzuzeigen
                loadPaketDetail(currentPaket.id.value)
                _state.update {
                    it.copy(message = "Paket wird gesendet", navigateToManager = true)
                }
                // Nach dem Start reload, um Hop-Count zu aktualisieren
                loadPaketDetail(currentPaket.id.value)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Fehler beim Teilen des Pakets: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleEditMode() {
        _state.update { it.copy(isEditing = !it.isEditing) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    // WiFi Direct Methoden

    fun toggleDiscovery() {
        if (_isDiscoveryActive.value) {
            stopDiscovery()
        } else {
            startDiscovery()
        }
    }

    private fun startDiscovery() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting device discovery")
                startBeaconingUseCase()
                _isDiscoveryActive.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error starting discovery", e)
                _state.update {
                    it.copy(error = "Fehler beim Starten der Gerätesuche: ${e.message}")
                }
            }
        }
    }

    private fun stopDiscovery() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Stopping device discovery")
                stopBeaconingUseCase()
                _isDiscoveryActive.value = false
            } catch (e: Exception) {
                // Fehler beim Stoppen der Discovery ist weniger kritisch
                Log.e(TAG, "Error stopping discovery", e)
            }
        }
    }

    fun shareWithDevice(deviceAddress: String) {
        val currentPaket = state.value.paket ?: return
        if (currentPaket.maxHops != null && currentPaket.currentHopCount >= currentPaket.maxHops) {
            _state.update { it.copy(error = "Maximale Weiterleitungen erreicht") }
            return
        }

        if (DEVICE_BLACKLIST.contains(deviceAddress)) {
            _state.update {
                it.copy(
                    error = "Dieses Gerät ist blockiert",
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Connecting to device: $deviceAddress")
                _state.update { it.copy(isLoading = true) }

                // Erstelle und speichere einen Peer-Eintrag bevor der Transfer gestartet wird
                val peerId = PeerId("direct-$deviceAddress")

                // Hole den Device-Namen aus der nearbyDevices-Liste
                val deviceName = nearbyDevices.value.find { it.deviceAddress == deviceAddress }?.deviceName ?: "Unbekanntes Gerät"

                // Erstelle einen Peer-Eintrag für dieses Gerät
                val peer = Peer(
                    id = peerId,
                    alias = deviceName,
                    lastSeenUtc = System.currentTimeMillis()
                )

                // Speichere den Peer in der Datenbank
                savePeerUseCase(peer)

                // Verbinde mit dem Gerät
                connectToDeviceUseCase(deviceAddress)

                // Warte bis die Verbindung steht (max 30 s)
                val connected = withTimeoutOrNull(30_000) {
                    observeConnectionStateUseCase()
                        .first { it.isConnected }
                }

                if (connected == null) {
                    Log.e(TAG, "Timeout – WiFi Direct Verbindung zu $deviceAddress nicht hergestellt")
                    throw IOException("WiFi Direct Verbindung wurde nicht hergestellt")
                }

                // Starte den Transfer
                startTransfer(currentPaket.id, peerId)

                _state.update {
                    it.copy(
                        isLoading = false,
                        message = "Transfer gestartet zu Gerät mit Adresse $deviceAddress",
                        navigateToManager = true
                    )
                }
                Log.d(TAG, "Transfer initiated to device: $deviceAddress")
            } catch (e: Exception) {
                Log.e(TAG, "Error sharing with device", e)
                _state.update {
                    it.copy(
                        error = "Fehler beim Teilen: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun DeviceInfo.toWifiP2pDevice(): WifiP2pDevice {
        return WifiP2pDevice().apply {
            deviceName = this@toWifiP2pDevice.deviceName ?: "Unbekanntes Gerät"
            deviceAddress = this@toWifiP2pDevice.deviceAddress
        }
    }

    fun clearNavigationFlag() { _state.update { it.copy(navigateToManager = false) } }

    override fun onCleared() {
        super.onCleared()
        stopDiscovery()
    }
}