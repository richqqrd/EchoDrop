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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
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
    private val _receivedManifests: MutableSharedFlow<Pair<PaketId, PeerId>> = MutableSharedFlow<Pair<PaketId, PeerId>>()
) : TransportManager {

    companion object {
        private const val TAG = "TransportManagerImpl"
        private const val MANIFEST_PREFIX = "MAN|"
        private const val CHUNK_PREFIX = "CHK|"
        private const val MAX_ATTEMPTS = 3
        private const val ATTEMPT_TIMEOUT = 60 * 60 * 1000L // 1 Stunde
        private const val CLEANUP_TIMEOUT = 24 * 60 * 60 * 1000L // 24 Stunden

        private val DEVICE_BLACKLIST = setOf(
            "f6:30:b9:4a:18:9d",
            "f6:30:b9:51:fe:4b",
            "a6:d7:3c:00:e8:ec",
            "0a:2e:5f:f1:00:b0"
        )
    }

    private val gson = Gson()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val incomingFrames = MutableSharedFlow<IncomingFrame>(replay = 0, extraBufferCapacity = 10)
    private val connectionAttempts = mutableMapOf<String, MutableList<ConnectionAttempt>>()

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

        coroutineScope.launch {
            while (isActive) {
                try {
                    val cutoffTime = System.currentTimeMillis() - CLEANUP_TIMEOUT
                    connectionAttemptRepository.cleanupOldAttempts(cutoffTime)
                    delay(CLEANUP_TIMEOUT)
                } catch (e: Exception) {
                    Log.e(TAG, "Error during connection attempts cleanup", e)
                }
            }
        }
    }

    override fun startBeaconing() {
        Log.d(TAG, "Starting beaconing")

        // Starte die Entdeckung von Geräten
        wifiDirectDiscovery.startDiscovery()

        // Starte den Service zum Empfangen von Daten
        wifiDirectService.startService()
    }

    override fun stopBeaconing() {
        Log.d(TAG, "Stopping beaconing")

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
        Log.d(TAG, "Sending paket ${paketId.value} to peer ${peerId.value}")

        try {
            val deviceAddress = if (peerId.value.startsWith("direct-")) {
                peerId.value.substringAfter("direct-")
            } else {
                return // Keine WiFi Direct Adresse
            }

                    // Blacklist-Prüfung hier hinzufügen
            if (DEVICE_BLACKLIST.contains(deviceAddress)) {
                Log.d(TAG, "Skipping send attempt - device $deviceAddress is blacklisted")
                return
            }

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

            transferRepository.startTransfer(paketId, peerId)

            val manifestJson = buildManifestForPaket(paketId.value)
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

    private suspend fun buildManifestForPaket(paketId: String): String {
        Log.d(TAG, "Building manifest for paket $paketId")

        try {
            // Hier getPaketDetailUseCase verwenden, nicht createPaketUseCase
            val paket = getPaketDetailUseCase(PaketId(paketId))
            if (paket == null) {
                Log.e(TAG, "Paket not found: $paketId")
                return "{\"paketId\": \"$paketId\", \"error\": \"Paket not found\"}"
            }

            val manifestData = JSONObject().apply {
                put("paketId", paketId)
                put("title", paket.meta.title)
                put("description", paket.meta.description)

                val tagsArray = JSONArray()
                paket.meta.tags.forEach { tag ->
                    tagsArray.put(tag)
                }
                put("tags", tagsArray)

                put("ttlSeconds", paket.meta.ttlSeconds)
                put("priority", paket.meta.priority)
                put("maxHops", paket.meta.maxHops ?: -1) // -1 bedeutet unbegrenzt
                put("currentHopCount", paket.currentHopCount) // Aktueller Hop-Count

                // Füge Datei-Metadaten hinzu
                val filesArray = JSONArray()
                paket.files.forEachIndexed { index, file ->
                    val fileName = File(file.path).name

                    // TODO file name auf empfänger noch falsch, aber richtig gepsiehcer in DB!
                    JSONObject().apply {
                        put("id", "file_${index}_${paketId}")
                        put("name", fileName)  // FileEntry hat 'path', nicht 'name'
                        put("size", file.sizeBytes)  // FileEntry hat 'sizeBytes', nicht 'size'
                        put("mimeType", file.mime)   // FileEntry hat 'mime', nicht 'mimeType'
                    }.also { filesArray.put(it) }
                }
                put("files", filesArray)
            }.toString()

            Log.d(TAG, "Created manifest: $manifestData")
            return manifestData
        } catch (e: Exception) {
            Log.e(TAG, "Error building manifest", e)
            // Fallback zu einem minimalen Manifest
            return "{\"paketId\": \"$paketId\", \"files\": []}"
        }
    }

    // Hilfsmethode zum Senden aller Chunks eines Pakets
    private suspend fun sendChunksForPaket(paketId: String, peerId: String) {
        // In einer vollständigen Implementation würdest du hier das Repository nutzen
        // und die tatsächlichen Datei-Chunks aus der Datenbank oder dem Dateisystem laden

        // Für einen ersten Test können wir einen Dummy-Chunk senden
        val dummyData = "Das ist ein Test-Chunk für Paket $paketId".toByteArray()
        val chunkId = "chunk1_$paketId"

        // Extrahiere die Zieladresse aus peerId (für WiFi Direct)
        val targetAddress = if (peerId.startsWith("direct-")) {
            peerId.substringAfter("direct-")
        } else {
            peerId // Falls es ein anderes Format ist
        }

        sendChunk(chunkId, dummyData)
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

                    // Manifest parsen und Paket erstellen
                    val paket = parseManifestAndCreatePaket(paketId, manifestJson, peerId)

                    // Speichere das Paket in der Datenbank
                    savePaketUseCase(paket)

                    if (paket.canBeForwarded()) {
                        Log.d(TAG, "Paket ${paket.id.value} kann weitergeleitet werden (Hop ${paket.currentHopCount}/${paket.maxHopCount})")
                        coroutineScope.launch {
                            delay(10000) // Warte 10 Sekunden vor der Weiterleitung
                            autoForwardPaket(paket)
                        }
                    } else {
                        Log.d(TAG, "Paket ${paket.id.value} hat maximale Hop-Anzahl erreicht (${paket.currentHopCount}/${paket.maxHopCount})")
                    }

                    // Erstelle ein IncomingFrame für das Manifest und emittiere es
                    val frame = IncomingFrame.Manifest(sourceAddress, paketId, manifestJson)
                    incomingFrames.emit(frame)

                    // Starte einen INCOMING Transfer im Repository
                    _receivedManifests.emit(Pair(PaketId(paketId), peerId))

                    // Setze den Transfer auf ACTIVE
                    progressCallback.updateProgress(PaketId(paketId), peerId, 10)
                    transferRepository.updateState(PaketId(paketId), peerId, TransferState.ACTIVE)
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
                        val fileId = if (idParts.size >= 3) idParts[2] else "file_0_$paketId"

                        // Speichere die Chunk-Daten in einer temporären Datei
                        val success = saveChunkToFile(PaketId(paketId), fileId, chunkData)

                        if (success) {
                            // Aktualisiere den Fortschritt im Repository
                            val peerId = PeerId("direct-$sourceAddress")
                            progressCallback.updateProgress(PaketId(paketId), peerId, 75)

                            // Prüfen, ob dies der letzte Chunk war (hier vereinfacht angenommen)
                            // In einer vollständigen Implementierung würdest du den Fortschritt verfolgen
                            val isLastChunk = true // Vereinfachte Annahme für das Beispiel

                            if (isLastChunk) {
                                progressCallback.updateProgress(PaketId(paketId), peerId, 100)
                                transferRepository.updateState(PaketId(paketId), peerId, TransferState.DONE)
                                Log.d(TAG, "Transfer completed for paket $paketId")
                            }

                            // Erstelle ein IncomingFrame für den Chunk und emittiere es
                            val frame = IncomingFrame.Chunk(sourceAddress, chunkId, chunkData)
                            incomingFrames.emit(frame)
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

    private fun parseManifestAndCreatePaket(paketId: String, manifestJson: String, peerId: PeerId): Paket {
        try {
            val jsonObject = JSONObject(manifestJson)

            // Extrahiere Metadaten
            val title = jsonObject.optString("title", "Unbekanntes Paket")
            val description = jsonObject.optString("description", "")

            val tagsArray = jsonObject.optJSONArray("tags")
            val tags = mutableListOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    tags.add(tagsArray.getString(i))
                }
            }
            val ttlSeconds = jsonObject.optInt("ttlSeconds", 3600)
            val priority = jsonObject.optInt("priority", 1)
            val maxHops = jsonObject.optInt("maxHops", -1).let { if (it == -1) null else it }
            val currentHopCount = jsonObject.optInt("currentHopCount", 0)

            // Erstelle Paket-Metadaten
            val meta = PaketMeta(
                title = title,
                description = description,
                tags = tags,
                ttlSeconds = ttlSeconds,
                priority = priority,
                maxHops = maxHops
            )

            // Verarbeite Dateien
            val files = mutableListOf<FileEntry>()
            val filesArray = jsonObject.optJSONArray("files")

            if (filesArray != null) {
                for (i in 0 until filesArray.length()) {
                    val fileObject = filesArray.getJSONObject(i)
                    val fileId = fileObject.getString("id")
                    val fileName = fileObject.getString("name")
                    val fileSize = fileObject.getLong("size")
                    val fileMime = fileObject.optString("mimeType", "application/octet-stream")

                    val filesDir = File(context.filesDir, "received_files")
                    if (!filesDir.exists()) {
                        filesDir.mkdirs()
                    }
                    val filePath = File(filesDir, "${paketId}_${fileId}_$fileName").absolutePath
                    // Füge die Datei hinzu
                    files.add(
                        FileEntry(
                            path = filePath,
                            mime = fileMime,
                            sizeBytes = fileSize,
                            orderIdx = i  // Verwende den Index aus der Schleife als orderIdx
                        )
                    )
                }
            }

            // Erstelle und gib das Paket zurück
            return Paket(
                id = PaketId(paketId),
                meta = meta,
                sizeBytes = files.sumOf { it.sizeBytes },  // Berechne die Gesamtgröße
                sha256 = "",  // Leerer SHA256-Wert für jetzt
                fileCount = files.size,  // Hier die Anzahl der Dateien angeben
                createdUtc = System.currentTimeMillis(),
                files = files,
                currentHopCount = currentHopCount,
                maxHopCount = maxHops
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing manifest JSON", e)
            // Erstelle ein minimales Paket als Fallback
            return Paket(
                id = PaketId(paketId),
                meta = PaketMeta(
                    title = "Empfangenes Paket",
                    description = "Fehler beim Parsen des Manifests",
                    tags = emptyList(),
                    ttlSeconds = 3600,
                    priority = 1
                ),
                sizeBytes = 0L,
                sha256 = "",
                fileCount = 0,
                createdUtc = System.currentTimeMillis(),
                files = emptyList()
            )
        }
    }

    private fun saveChunkToFile(paketId: PaketId, fileId: String, data: ByteArray): Boolean {
        try {
            // Erstelle einen Dateinamen basierend auf der Paket-ID und Datei-ID
            val fileName = "${paketId.value}_${fileId.replace('/', '_')}.bin"

            // Verwende einen dauerhaften Speicherort statt des Cache-Verzeichnisses
            val filesDir = File(context.filesDir, "received_files")
            if (!filesDir.exists()) {
                filesDir.mkdirs()
            }

            val file = File(filesDir, fileName)

            // Schreibe die Daten in die Datei
            FileOutputStream(file).use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
            }

            Log.d(TAG, "Successfully saved chunk data to ${file.absolutePath}")

            // Starte eine Coroutine zum Aktualisieren des Dateipfads
            coroutineScope.launch {
                updateFilePathInPaket(paketId, fileId, file.absolutePath)
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chunk data to file", e)
            return false
        }
    }

    // Neue Methode zum Aktualisieren des Dateipfads im Paket
    private suspend fun updateFilePathInPaket(paketId: PaketId, fileId: String, absolutePath: String) {
        try {
            // Lade das aktuelle Paket
            val paket = getPaketDetailUseCase(paketId) ?: return

            // Finde und aktualisiere die entsprechende Datei
            val updatedFiles = paket.files.map { file ->
                if (file.path.contains(fileId)) {
                    file.copy(path = absolutePath)
                } else {
                    file
                }
            }

            // Erstelle ein aktualisiertes Paket
            val updatedPaket = paket.copy(files = updatedFiles)

            // Speichere das aktualisierte Paket
            savePaketUseCase(updatedPaket)

            Log.d(TAG, "Updated file path in paket $paketId for file $fileId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating file path in paket", e)
        }
    }

    private suspend fun autoForwardPaket(paket: Paket) {
        try {
            // 1. Starte Discovery, um Geräte zu finden
            startDiscovery()

            delay(5000) // Warte 5 Sekunden, um Geräte zu entdecken

            // 2. Hole die Liste der aktuell entdeckten Geräte
            val devices = wifiDirectDiscovery.discoveredDevices.value

            if (devices.isEmpty()) {
                Log.d(TAG, "Keine Geräte zum Weiterleiten von Paket ${paket.id.value} gefunden")
                return
            }

            Log.d(TAG, "Versuche, Paket ${paket.id.value} an ${devices.size} Geräte weiterzuleiten")

            // 3. Inkrementiere den Hop-Counter
            val forwardedPaket = paket.incrementHopCount()
            savePaketUseCase(forwardedPaket)

            // 4. Sende das Paket an jedes Gerät
            devices.forEach { device ->
                try {
                    // Verbinde mit dem Gerät
                    connectToPeer(device.deviceAddress)

                    // Warte auf Verbindungsaufbau
                    delay(7000)

                    // Prüfe Verbindungsstatus
                    val connectionState = wifiDirectDiscovery.connectionInfo.value
                    if (connectionState != null && connectionState.groupFormed) {
                        // Erstelle einen Peer-Eintrag
                        val peerId = PeerId("direct-${device.deviceAddress}")

                        // Sende das Paket
                        sendPaket(forwardedPaket.id, peerId)

                        Log.d(TAG, "Paket ${forwardedPaket.id.value} erfolgreich an ${device.deviceName} weitergeleitet")

                        // Warte nach dem Senden und trenne die Verbindung
                        delay(5000)
                        disconnectDevice()
                    } else {
                        Log.d(TAG, "Keine Verbindung zu ${device.deviceName} möglich für Weiterleitung")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Fehler beim Weiterleiten an ${device.deviceName}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fehler bei automatischer Weiterleitung: ${e.message}")
        } finally {
            // Beende Discovery nach der Weiterleitung
            stopDiscovery()
        }
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
}