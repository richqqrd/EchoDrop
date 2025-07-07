package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import android.util.Log
import com.example.echodrop.model.domainLayer.model.ConnectionState
import com.example.echodrop.model.domainLayer.model.DeviceInfo
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import com.example.echodrop.model.domainLayer.transport.IncomingFrame
import com.example.echodrop.model.domainLayer.transport.TransferProgressCallback
import com.example.echodrop.model.domainLayer.transport.TransportManager
import com.example.echodrop.model.dataLayer.transport.WiFiDirectDiscovery
import com.example.echodrop.model.dataLayer.transport.WiFiDirectService
import com.example.echodrop.model.domainLayer.model.ConnectionAttempt
import com.example.echodrop.model.domainLayer.repository.ConnectionAttemptRepository
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import com.example.echodrop.model.domainLayer.usecase.paket.GetPaketDetailUseCase
import com.example.echodrop.model.domainLayer.usecase.paket.SavePaketUseCase
import com.example.echodrop.model.domainLayer.usecase.peer.SavePeerUseCase
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.echodrop.model.domainLayer.transport.ManifestBuilder
import com.example.echodrop.model.domainLayer.transport.ManifestParser
import com.example.echodrop.model.domainLayer.transport.ChunkIO
import kotlinx.coroutines.Job
import com.example.echodrop.model.domainLayer.transport.Forwarder
import com.example.echodrop.model.domainLayer.transport.MaintenanceScheduler
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransportManagerImpl @Inject constructor(
    private val wifiDirectService: WiFiDirectService,
    private val wifiDirectDiscovery: WiFiDirectDiscovery,
    private val transferRepository: TransferRepository,
    private val connectionAttemptRepository: ConnectionAttemptRepository,
    private val peerRepository: PeerRepository,
    @ApplicationContext private val context: Context,
    private val savePaketUseCase: SavePaketUseCase,
    private val getPaketDetailUseCase: GetPaketDetailUseCase,
    private val progressCallback: TransferProgressCallback,
    private val manifestBuilder: ManifestBuilder,
    private val manifestParser: ManifestParser,
    private val _receivedManifests: MutableSharedFlow<Pair<PaketId, PeerId>> = MutableSharedFlow(),
    private val chunkIO: ChunkIO,
    private val forwarder: Forwarder,
    private val maintenanceScheduler: MaintenanceScheduler
) : TransportManager {

    companion object {
        private const val TAG = "TransportManagerImpl"
        private const val MANIFEST_PREFIX = "MAN|"
        private const val CHUNK_PREFIX = "CHK|"
        private const val MAX_ATTEMPTS = 3
        private const val ATTEMPT_TIMEOUT = 60 * 60 * 1000L // 1 Stunde

        private val DEVICE_BLACKLIST = setOf(
            "f6:30:b9:4a:18:9d",
            "f6:30:b9:51:fe:4b",
            "a6:d7:3c:00:e8:ec",
            "0a:2e:5f:f1:00:b0",
            "66:07:f6:75:63:19"
        )
    }

    private val gson = Gson()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val incomingFrames = MutableSharedFlow<IncomingFrame>(replay = 0, extraBufferCapacity = 10)
    private val connectionAttempts = mutableMapOf<String, MutableList<ConnectionAttempt>>()

    // Liste aller noch laufenden Auto-Forward Jobs, damit wir sie bei Beacon-Stopp abbrechen können
    private val forwardJobs = mutableListOf<Job>()

    /**
     * Flag, ob Beaconing (und damit ausdrücklich das automatische Weiterleiten) aktiv ist.
     * Wird von startBeaconing/stopBeaconing gesetzt und von processReceivedData geprüft.
     */
    @Volatile
    private var beaconingActive: Boolean = false

    private val receivedBytesMap = mutableMapOf<String, Long>()

    init {
        // Beobachte eingehende Daten vom WiFiDirectService
        coroutineScope.launch {
            wifiDirectService.observeIncomingData().collect { (sourceAddress, data) ->
                try {
                    val dataString = String(data)
                    processReceivedData(sourceAddress, dataString, data)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing incoming data", e)
                }
            }
        }

        // Beobachte Verbindungsänderungen
        coroutineScope.launch {
            wifiDirectDiscovery.connectionInfo.collect { connectionInfo ->
                if (connectionInfo != null) {
                    wifiDirectService.onConnectionEstablished(connectionInfo)
                }
            }
        }

        coroutineScope.launch {
            wifiDirectService.observeTransferProgress().collect { (paketId, address, progress) ->
                val peerId = PeerId("direct-$address")
                progressCallback.updateProgress(paketId, peerId, progress)
            }
        }

        // Wartungs-Aufgaben werden nun vom MaintenanceScheduler erledigt
    }

    override fun startBeaconing() {
        Log.d(TAG, "Starting beaconing")

        beaconingActive = true

        // Starte die Entdeckung von Geräten
        wifiDirectDiscovery.startDiscovery()

        // Starte den Service zum Empfangen von Daten
        wifiDirectService.startService()
    }

    override fun stopBeaconing() {
        Log.d(TAG, "Stopping beaconing")

        beaconingActive = false

        // Laufende Auto-Forward-Jobs abbrechen
        synchronized(forwardJobs) {
            forwardJobs.forEach { it.cancel() }
            forwardJobs.clear()
        }

        // Stoppe alle WiFi Direct-Aktivitäten
        wifiDirectDiscovery.stopDiscovery()
        wifiDirectService.stopService()
    }

    override suspend fun sendManifest(paketId: String, manifestJson: String) {
        Log.d(TAG, "Sending manifest for paket $paketId")

        // Formatiere Manifest-Daten mit Präfix und Paket-ID
        val dataToSend = "$MANIFEST_PREFIX$paketId|$manifestJson"

        // Prüfe Verbindungsstatus explizit
        val connectionState = wifiDirectDiscovery.connectionInfo.value
        if (connectionState == null || !connectionState.groupFormed) {
            throw IllegalStateException("No active WiFi Direct connection. Please ensure devices are connected.")
        }

        val targetAddress = connectionState.groupOwnerAddress?.hostAddress
            ?: throw IllegalStateException("No connected device to send data to")

        Log.d(TAG, "Using target address: $targetAddress for group owner: ${connectionState.isGroupOwner}")

        // Sende die Daten über den Service
        wifiDirectService.sendData(
            dataToSend.toByteArray(),
            targetAddress,
            PaketId(paketId)
        )
    }

    override suspend fun sendChunk(chunkId: String, data: ByteArray) {
        Log.d(TAG, "Sending chunk $chunkId")

        // Aktueller Verbindungspartner
        val connectionState = wifiDirectDiscovery.connectionInfo.value
        val targetAddress = connectionState?.groupOwnerAddress?.hostAddress
            ?: throw IllegalStateException("No connected device to send data to")

        // Extrahiere die Paket-ID aus der Chunk-ID (Annahme: Format "chunkX_paketId")
        val paketId = chunkId.substringAfter("_")

        // Erstelle das Präfix für den Chunk
        val prefix = "$CHUNK_PREFIX$chunkId|".toByteArray()

        // Kombiniere Präfix und Daten
        val combined = ByteArray(prefix.size + data.size)
        System.arraycopy(prefix, 0, combined, 0, prefix.size)
        System.arraycopy(data, 0, combined, prefix.size, data.size)

        // Sende die kombinierten Daten
        wifiDirectService.sendData(combined, targetAddress, PaketId(paketId))
    }

    override fun observeIncoming(): Flow<IncomingFrame> {
        return incomingFrames
    }

    override suspend fun startDiscovery() {
        Log.d(TAG, "Starting device discovery")
        wifiDirectDiscovery.startDiscovery()
    }

    override suspend fun stopDiscovery() {
        Log.d(TAG, "Stopping device discovery")
        wifiDirectDiscovery.stopDiscovery()
    }

    override suspend fun connectToDevice(deviceAddress: String) {
        Log.d(TAG, "Connecting to device: $deviceAddress")
        wifiDirectDiscovery.connectToDevice(deviceAddress)
    }

    override fun observeDiscoveredDevices(): Flow<List<DeviceInfo>> {
        return wifiDirectDiscovery.discoveredDevices
            .map { devices ->
                devices.map { device ->
                    DeviceInfo(
                        deviceName = device.deviceName,
                        deviceAddress = device.deviceAddress
                    )
                }
            }
    }

    override fun observeThisDevice(): Flow<DeviceInfo?> {
        return wifiDirectDiscovery.thisDevice
            .map { device ->
                device?.let {
                    DeviceInfo(
                        deviceName = it.deviceName,
                        deviceAddress = it.deviceAddress
                    )
                }
            }
    }

    override fun observeConnectionState(): Flow<ConnectionState> {
        return wifiDirectDiscovery.connectionInfo
            .map { info ->
                ConnectionState(
                    isConnected = info != null && info.groupFormed,
                    connectedDevices = if (info?.groupFormed == true)
                        setOf(info.groupOwnerAddress?.hostAddress ?: "")
                    else
                        emptySet(),
                    groupOwnerAddress = info?.groupOwnerAddress?.hostAddress,
                    isGroupOwner = info?.isGroupOwner ?: false
                )
            }
    }

    override suspend fun sendPaket(paketId: PaketId, peerId: PeerId) {
        try {
            val deviceAddress = if (peerId.value.startsWith("direct-")) {
                peerId.value.substringAfter("direct-")
            } else {
                return // Keine WiFi Direct Adresse
            }

            // Prüfe Blacklist **vor** jeglichen weiteren Aktionen
            if (DEVICE_BLACKLIST.contains(deviceAddress)) {
                Log.d(TAG, "Device $deviceAddress is blacklisted – not sending paket ${paketId.value}")
                return
            }

            // Prüfe Hop-Limit
            val paket = getPaketDetailUseCase(paketId) ?: run {
                Log.e(TAG, "Paket ${paketId.value} nicht gefunden – Abbruch")
                return
            }

            if (!paket.canBeForwarded()) {
                Log.d(TAG, "Paket ${paketId.value} hat maximale Hop-Anzahl erreicht – nicht senden")
                return
            }

            // Hop-Counter erhöhen und speichern
            val forwarded = paket.incrementHopCount()
            savePaketUseCase(forwarded)

            // Stelle sicher, dass ein Peer-Eintrag existiert (erforderlich für FK in transfer_log)
            val peerIdEntity = PeerId("direct-$deviceAddress")
            val minimalPeer = Peer(
                id = peerIdEntity,
                alias = "WiFi Direct Peer",
                lastSeenUtc = System.currentTimeMillis()
            )
            peerRepository.upsertPeer(minimalPeer)

            // Erst jetzt loggen, dass der Versand tatsächlich versucht wird
            Log.d(TAG, "Sending paket ${paketId.value} to peer ${peerId.value}")

            if (isDeviceAlreadyTried(deviceAddress, paketId)) {
                Log.d(TAG, "Skipping send attempt - max retries reached for device $deviceAddress")
                return
            }

            val connectionState = wifiDirectDiscovery.connectionInfo.value
            if (connectionState == null || !connectionState.groupFormed) {
                Log.e(TAG, "No active WiFi Direct connection, cannot send paket")
                connectionAttemptRepository.trackAttempt(deviceAddress, paketId, false)
                return
            }

            transferRepository.startTransfer(paketId, peerId, com.example.echodrop.model.domainLayer.model.TransferDirection.OUTGOING)

            val manifestJson = manifestBuilder.build(paketId)
            sendManifest(paketId.value, manifestJson)
            sendChunksForPaket(paketId.value, peerId.value)

            connectionAttemptRepository.trackAttempt(deviceAddress, paketId, true)
            transferRepository.updateState(paketId, peerId, TransferState.DONE)

        } catch (e: Exception) {
            Log.e(TAG, "Error sending paket ${paketId.value}", e)
            
            if (peerId.value.startsWith("direct-")) {
                val deviceAddress = peerId.value.substringAfter("direct-")
                connectionAttemptRepository.trackAttempt(deviceAddress, paketId, false)
            }
            
            transferRepository.updateState(paketId, peerId, TransferState.FAILED)
            throw e
        }
    }

    private suspend fun sendChunksForPaket(paketId: String, peerId: String) {
        // Lade Paket inkl. Dateien
        val paket = getPaketDetailUseCase(PaketId(paketId)) ?: run {
            Log.e(TAG, "Paket $paketId nicht gefunden – breche Chunk-Versand ab")
            return
        }

        // Iteriere über Dateien
        paket.files.forEachIndexed { idx, entry ->
            val f = File(entry.path)
            if (!f.exists()) {
                Log.e(TAG, "Datei ${f.absolutePath} existiert nicht – überspringe")
                return@forEachIndexed
            }
            val fileId = "file_${idx}_${paketId}"
            chunkIO.splitFile(f, "${paketId}_${fileId}").forEach { (chunkId, bytes) ->
                Log.d(TAG, "Sende Chunk $chunkId (${bytes.size}B) an $peerId")
                sendChunk(chunkId, bytes)
            }
        }
    }

    // Diese Hilfsklasse sollte auch definiert werden
    private data class Chunk(val id: String, val data: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Chunk

            if (id != other.id) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }

    // Dummy-Implementierung für Test-Zwecke
    private fun getChunksForPaket(paketId: String): List<Chunk> {
        // Erstelle einen Dummy-Chunk für Tests
        val dummyData = "Das ist ein Test-Chunk für Paket $paketId".toByteArray()
        return listOf(Chunk("chunk1_$paketId", dummyData))
    }

    /**
     * Verarbeitet eingehende Daten und konvertiert sie in IncomingFrame-Objekte
     */
    private suspend fun processReceivedData(sourceAddress: String, dataAsString: String, data: ByteArray) {
        try {
            // Prüfe, ob es sich um ein Manifest handelt
            if (dataAsString.startsWith(MANIFEST_PREFIX)) {
                val content = dataAsString.substring(MANIFEST_PREFIX.length)
                val parts = content.split("|", limit = 2)

                if (parts.size == 2) {
                    val paketId = parts[0]
                    val manifestJson = parts[1]

                    Log.d(TAG, "Received manifest for paket $paketId")

                    // Erstelle einen Peer-Eintrag für den Absender
                    val peerId = PeerId("direct-$sourceAddress")
                    val peer = Peer(
                        id = peerId,
                        alias = "WiFi Direct Gerät",
                        lastSeenUtc = System.currentTimeMillis()
                    )
                    peerRepository.upsertPeer(peer)

                    // Manifest in Paket umwandeln
                    val paket = manifestParser.parse(paketId, manifestJson, peerId)

                    // Speichere das Paket in der Datenbank
                    savePaketUseCase(paket)

                    if (paket.canBeForwarded()) {
                        if (beaconingActive) {
                            Log.d(TAG, "[Forward] Paket ${paket.id.value} wird in 10s weitergeleitet (Hop ${paket.currentHopCount}/${paket.maxHopCount}). Beaconing ist aktiv.")
                            val job = coroutineScope.launch {
                                delay(10_000) // Kurze Pause, um UI/Transfers zu initialisieren
                                autoForwardPaket(paket, sourceAddress)
                            }
                            synchronized(forwardJobs) { forwardJobs += job }
                        } else {
                            Log.d(TAG, "[Forward] Paket ${paket.id.value} KÖNNTE weitergeleitet werden, aber Beaconing ist nicht aktiv – überspringe.")
                        }
                    } else {
                        Log.d(TAG, "[Forward] Paket ${paket.id.value} hat maximale Hop-Anzahl erreicht (${paket.currentHopCount}/${paket.maxHopCount})")
                    }

                    // Erstelle ein IncomingFrame für das Manifest und emittiere es
                    val frame = IncomingFrame.Manifest(sourceAddress, paketId, manifestJson)
                    incomingFrames.emit(frame)

                    // 1) Persistiere einen TransferLog-Eintrag, damit UI/Fortschritt sichtbar wird
                    transferRepository.startTransfer(PaketId(paketId), peerId)

                    // 2) Optionale Notification über SharedFlow
                    _receivedManifests.emit(Pair(PaketId(paketId), peerId))

                    // Setze den Transfer auf ACTIVE
                    progressCallback.updateProgress(PaketId(paketId), peerId, 10)
                    // startTransfer hat den Status bereits auf ACTIVE gesetzt, daher kein zweites Update nötig
                }
            }
            // Prüfe, ob es sich um einen Chunk handelt
            else if (dataAsString.startsWith(CHUNK_PREFIX)) {
                val headerEndIndex = dataAsString.indexOf('|', CHUNK_PREFIX.length)
                if (headerEndIndex != -1) {
                    val chunkId = dataAsString.substring(CHUNK_PREFIX.length, headerEndIndex)
                    val chunkData = data.copyOfRange(headerEndIndex + 1, data.size)

                    Log.d(TAG, "Received chunk $chunkId with ${chunkData.size} bytes")

                    // Extrahiere die Paket-ID und Datei-ID aus der Chunk-ID
                    // Format: "chunkX_paketId_fileId"
                    val idParts = chunkId.split("_")
                    if (idParts.size >= 2) {
                        val paketId = idParts[1]
                        // fileId kann selbst Unterstriche enthalten (z. B. "file_0_<paket>") -> alle restlichen Teile wieder zusammenfügen
                        val fileId = if (idParts.size >= 3) idParts.subList(2, idParts.size).joinToString("_") else "file_0_$paketId"

                        // Speichere die Chunk-Daten in einer temporären Datei
                        val success = chunkIO.appendChunk(PaketId(paketId), fileId, chunkData)
                        if (success) {
                            val peerId = PeerId("direct-$sourceAddress")
                            
                            // Lade das aktuelle Paket
                            val paket = getPaketDetailUseCase(PaketId(paketId))
                            if (paket != null) {
                                // Aktualisiere empfangene Bytes für dieses Paket
                                val currentReceived = receivedBytesMap.getOrDefault(paketId, 0L)
                                val newReceived = currentReceived + chunkData.size
                                receivedBytesMap[paketId] = newReceived

                                // Berechne den tatsächlichen Fortschritt
                                val totalBytes = paket.sizeBytes
                                val currentProgress = if (totalBytes > 0) {
                                    ((newReceived * 100) / totalBytes).toInt()
                                } else {
                                    50 // Fallback wenn keine Größe bekannt
                                }

                                // Verhindere, dass ein höherer Fortschritt (z. B. 100 %) durch einen niedrigeren Wert überschrieben wird
                                val previousProgress = receivedBytesMap.getOrDefault("${paketId}_lastProgress", 0L).toInt()
                                val effectiveProgress = maxOf(currentProgress, previousProgress)

                                Log.d(TAG, "Paket $paketId: Empfangen $newReceived von $totalBytes Bytes ($currentProgress%), wir melden $effectiveProgress%")
                                progressCallback.updateProgress(PaketId(paketId), peerId, effectiveProgress)

                                // Merke uns den zuletzt gemeldeten Fortschritt
                                receivedBytesMap["${paketId}_lastProgress"] = effectiveProgress.toLong()

                                // Wenn alle Bytes empfangen wurden
                                if (newReceived >= totalBytes) {
                                    Log.d(TAG, "Paket $paketId vollständig empfangen, setze auf 100%")
                                    progressCallback.updateProgress(PaketId(paketId), peerId, 100)
                                    transferRepository.updateState(PaketId(paketId), peerId, TransferState.DONE)
                                    // Cleanup
                                    receivedBytesMap.remove(paketId)
                                    Log.d(TAG, "Transfer completed for paket $paketId")
                                }

                                // Erstelle ein IncomingFrame für den Chunk und emittiere es
                                val frame = IncomingFrame.Chunk(sourceAddress, chunkId, chunkData)
                                incomingFrames.emit(frame)
                            } else {
                                Log.e(TAG, "Paket $paketId nicht gefunden für Fortschrittsberechnung")
                            }
                        }
                    }
                }
            }
            // Unbekanntes Format
            else {
                Log.d(TAG, "Received unknown data format from $sourceAddress")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing received data", e)
        }
    }

    /**
     * Verbindet mit einem Peer unter Verwendung der WiFiDirectDiscovery
     */
    fun connectToPeer(deviceAddress: String) {
        wifiDirectDiscovery.connectToDevice(deviceAddress)
    }

    private suspend fun autoForwardPaket(paket: Paket, excludeAddress: String? = null) {
        if (!beaconingActive) {
            Log.d(TAG, "[Forward] Beaconing inactive – breche Weiterleitung ab")
            return
        }

        // Delegiere an Forwarder-Komponente
        forwarder.autoForward(paket, excludeAddress)
    }

    /**
     * Wartet asynchron bis das Gerät einer WiFi-Direct-Gruppe beigetreten ist.
     * @return true wenn groupFormed innerhalb der Wartezeit, sonst false
     */
    private suspend fun waitForGroup(maxWaitMs: Long = 20_000): Boolean {
        val start = System.currentTimeMillis()
        while (wifiDirectDiscovery.connectionInfo.value?.groupFormed != true) {
            if (System.currentTimeMillis() - start > maxWaitMs) return false
            delay(500)
        }
        return true
    }

    private suspend fun waitForNoGroup(maxWaitMs: Long = 15_000): Boolean {
        val start = System.currentTimeMillis()
        while (wifiDirectDiscovery.connectionInfo.value?.groupFormed == true) {
            if (System.currentTimeMillis() - start > maxWaitMs) return false
            delay(500)
        }
        return true
    }

    override suspend fun disconnectDevice() {
        try {
            Log.d(TAG, "Disconnecting from current WiFi Direct group")
            wifiDirectDiscovery.disconnectFromCurrentGroup()
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting device", e)
        }
    }

    override suspend fun forwardPaket(paketId: PaketId) {
        try {
            val paket = getPaketDetailUseCase(paketId) ?: throw IllegalArgumentException("Paket not found")
            Log.d(TAG, "Manually forwarding paket ${paketId.value}")
            autoForwardPaket(paket)
        } catch (e: Exception) {
            Log.e(TAG, "Error forwarding paket", e)
            throw e
        }
    }

    private suspend fun isDeviceAlreadyTried(deviceAddress: String, paketId: PaketId): Boolean {
        val minTimestamp = System.currentTimeMillis() - ATTEMPT_TIMEOUT
        val failedAttempts = connectionAttemptRepository.getFailedAttemptCount(
            deviceAddress,
            paketId,
            minTimestamp
        )
        return failedAttempts >= MAX_ATTEMPTS
    }

    override fun observeForwardEvents() = forwarder.events
}