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
import java.io.FileInputStream
import java.util.concurrent.TimeUnit
import com.example.echodrop.model.domainLayer.transport.ForwardEvent
import kotlinx.coroutines.Job

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
    private val _receivedManifests: MutableSharedFlow<Pair<PaketId, PeerId>> = MutableSharedFlow<Pair<PaketId, PeerId>>(),
    private val _forwardEvents: MutableSharedFlow<ForwardEvent> = MutableSharedFlow<ForwardEvent>(replay = 0, extraBufferCapacity = 20)
) : TransportManager {

    companion object {
        private const val TAG = "TransportManagerImpl"
        private const val MANIFEST_PREFIX = "MAN|"
        private const val CHUNK_PREFIX = "CHK|"
        private const val CHUNK_SIZE = 64 * 1024 // 64 KB – muss <= WiFiDirectService.BUFFER_SIZE sein
        private const val MAX_ATTEMPTS = 3
        private const val ATTEMPT_TIMEOUT = 60 * 60 * 1000L // 1 Stunde
        private const val CLEANUP_TIMEOUT = 24 * 60 * 60 * 1000L // 24 Stunden

        private val receivedBytesMap = mutableMapOf<String, Long>()


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

        // Zusätzlich: Bereinige veraltete Peers (nicht gesehen > 7 Tage)
        coroutineScope.launch {
            val interval = TimeUnit.HOURS.toMillis(6) // alle 6 h prüfen
            val peerCutoff = TimeUnit.DAYS.toMillis(7) // 7 Tage
            while (isActive) {
                try {
                    val deleted = peerRepository.purgeStalePeers(System.currentTimeMillis() - peerCutoff)
                    Log.d(TAG, "Peer cleanup removed $deleted stale peer(s)")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during peer cleanup", e)
                }
                delay(interval)
            }
        }
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

    // Hilfsmethode: Sende alle Dateien eines Pakets stückweise an den verbundenen Peer
    private suspend fun sendChunksForPaket(paketId: String, peerId: String) {
        // Lade Paket inkl. Dateien
        val paket = getPaketDetailUseCase(PaketId(paketId)) ?: run {
            Log.e(TAG, "Paket $paketId nicht gefunden – breche Chunk-Versand ab")
            return
        }

        // Iteriere über Dateien
        paket.files.forEachIndexed { fileIdx, fileEntry ->
            val file = File(fileEntry.path)
            if (!file.exists()) {
                Log.e(TAG, "Datei ${file.absolutePath} existiert nicht – überspringe")
                return@forEachIndexed
            }

            val fileId = "file_${fileIdx}_${paketId}"

            file.inputStream().use { input ->
                val buffer = ByteArray(CHUNK_SIZE)
                var bytesRead: Int
                var chunkIdx = 0
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    val dataToSend = if (bytesRead == CHUNK_SIZE) buffer else buffer.copyOf(bytesRead)

                    val chunkId = "chunk${chunkIdx}_${paketId}_${fileId}"
                    Log.d(TAG, "Sende Chunk $chunkId (${bytesRead}B) an $peerId")

                    sendChunk(chunkId, dataToSend)

                    chunkIdx++
                }
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

                    // Manifest parsen und Paket erstellen
                    val paket = parseManifestAndCreatePaket(paketId, manifestJson, peerId)

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
                        val success = saveChunkToFile(PaketId(paketId), fileId, chunkData)
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

    private suspend fun saveChunkToFile(paketId: PaketId, fileId: String, data: ByteArray): Boolean {
        return try {
            // Versuche den bereits in der DB hinterlegten Dateipfad zu ermitteln
            val paket = getPaketDetailUseCase(paketId)
            val fileEntry = paket?.files?.firstOrNull { it.path.contains(fileId) }

            // Fallback-Pfad falls Manifest/DB (noch) nicht vorhanden
            val file: File = if (fileEntry != null) {
                File(fileEntry.path)
            } else {
                // Gleiche Logik wie in parseManifestAndCreatePaket, aber ohne Dateinamen → verwende *.part
                val filesDir = File(context.filesDir, "received_files")
                if (!filesDir.exists()) filesDir.mkdirs()
                File(filesDir, "${paketId.value}_${fileId.replace('/', '_')}.part")
            }

            // Stelle sicher, dass Verzeichnisse existieren
            file.parentFile?.takeIf { !it.exists() }?.mkdirs()

            // Chunk anhängen (append = true)
            FileOutputStream(file, /*append=*/ true).use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
            }

            if (fileEntry == null) {
                // Wenn wir nur einen Fallback-Pfad hatten, aktualisiere jetzt den Pfad in der DB,
                // damit die UI später den korrekten Namen kennt.
                coroutineScope.launch {
                    updateFilePathInPaket(paketId, fileId, file.absolutePath)
                }
            }

            Log.d(TAG, "Successfully appended ${data.size} B to ${file.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chunk data to file", e)
            false
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

    private suspend fun autoForwardPaket(paket: Paket, excludeAddress: String? = null) {
        if (!beaconingActive) {
            Log.d(TAG, "[Forward] Beaconing inactive – breche Weiterleitung ab")
            return
        }

        try {
            Log.d(TAG, "[Forward] Starte automatische Weiterleitung für Paket ${paket.id.value}")

            // 1. Starte Discovery (falls nicht bereits aktiv)
            startDiscovery()

            delay(5_000) // Gib der Peer-Discovery etwas Zeit

            // 2. Geräte ermitteln & filtern
            val allDevices = wifiDirectDiscovery.discoveredDevices.value
            val candidates = allDevices
                .filterNot { DEVICE_BLACKLIST.contains(it.deviceAddress) }
                .filterNot { excludeAddress != null && it.deviceAddress == excludeAddress }
                .shuffled()
                .take(3) // max. 3 Versuche pro Weiterleitung

            if (candidates.isEmpty()) {
                Log.d(TAG, "[Forward] Keine geeigneten Peers gefunden – Abbruch")
                return
            }

            // 3. Hop-Counter erhöhen und speichern, bevor wir senden
            val forwardedPaket = paket.incrementHopCount()
            savePaketUseCase(forwardedPaket)

            // 4. Iteriere über Kandidaten
            for (device in candidates) {
                if (!beaconingActive) {
                    Log.d(TAG, "[Forward] Beaconing wurde gestoppt – breche Schleife ab")
                    break
                }
                val devAddr = device.deviceAddress

                // Falls wir noch in einer bestehenden Gruppe sind, zuerst trennen und warten
                if (wifiDirectDiscovery.connectionInfo.value?.groupFormed == true) {
                    Log.d(TAG, "[Forward] Bestehende Gruppe erkannt – trenne, bevor ich zu $devAddr verbinde")
                    disconnectDevice()
                    val leftGroup = waitForNoGroup()
                    if (!leftGroup) {
                        Log.d(TAG, "[Forward] Konnte Gruppe nicht rechtzeitig verlassen – überspringe $devAddr")
                        continue
                    }

                    // Neue Peer-Discovery starten, damit das zuvor verlassene Gerät uns wieder sieht
                    Log.d(TAG, "[Forward] Starte erneute Peer-Discovery nach Gruppen-Trennung")
                    startDiscovery()
                    delay(4_000) // gib Android Zeit, neue Peers zu liefern
                }

                // Retry-Limit prüfen
                if (isDeviceAlreadyTried(devAddr, forwardedPaket.id)) {
                    Log.d(TAG, "[Forward] $devAddr bereits $MAX_ATTEMPTS× erfolglos – überspringe")
                    continue
                }

                Log.d(TAG, "[Forward] Verbinde zu ${device.deviceName} ($devAddr)")
                _forwardEvents.emit(
                    ForwardEvent(
                        paketId = forwardedPaket.id,
                        peerId = PeerId("direct-$devAddr"),
                        stage = ForwardEvent.Stage.CONNECTING,
                        message = "Verbinde zu ${device.deviceName}"
                    )
                )

                try {
                    connectToPeer(devAddr)

                    // Warte, bis die Gruppe steht
                    val connected = waitForGroup()
                    if (!connected) {
                        Log.d(TAG, "[Forward] Timeout – Gruppe mit $devAddr nicht aufgebaut")
                        _forwardEvents.emit(
                            ForwardEvent(forwardedPaket.id, PeerId("direct-$devAddr"), ForwardEvent.Stage.TIMEOUT, "Timeout bei Verbindung")
                        )
                        connectionAttemptRepository.trackAttempt(devAddr, forwardedPaket.id, false)
                        continue
                    }

                    // PeerId anlegen
                    val peerId = PeerId("direct-$devAddr")

                    // Paket senden
                    sendPaket(forwardedPaket.id, peerId)
                    _forwardEvents.emit(
                        ForwardEvent(forwardedPaket.id, peerId, ForwardEvent.Stage.SENT, "Paket gesendet")
                    )

                    connectionAttemptRepository.trackAttempt(devAddr, forwardedPaket.id, true)

                    Log.d(TAG, "[Forward] Paket ${forwardedPaket.id.value} erfolgreich an ${device.deviceName} weitergeleitet")

                    // Nachgelagertes Disconnect
                    disconnectDevice()
                } catch (e: Exception) {
                    Log.e(TAG, "[Forward] Fehler beim Weiterleiten an ${device.deviceName}: ${e.message}")
                    _forwardEvents.emit(
                        ForwardEvent(forwardedPaket.id, PeerId("direct-$devAddr"), ForwardEvent.Stage.FAILED, e.message ?: "Unbekannter Fehler")
                    )
                    connectionAttemptRepository.trackAttempt(devAddr, forwardedPaket.id, false)
                    disconnectDevice()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Forward] Unerwarteter Fehler: ${e.message}", e)
        } finally {
            // Discovery beenden, damit UI-State sauber bleibt
            stopDiscovery()
        }
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

    override fun observeForwardEvents() = _forwardEvents
}