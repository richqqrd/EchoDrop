package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import android.net.wifi.p2p.WifiP2pInfo
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.example.echodrop.model.domainLayer.model.PaketId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Verantwortlich für die Socket-Kommunikation über WiFi Direct.
 * Verwaltet Server- und Client-Sockets, um Daten zwischen verbundenen Geräten zu übertragen.
 */
@Singleton
class WiFiDirectService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WiFiDirectService"
        private const val PORT = 8988
        private const val BUFFER_SIZE = 1024 * 64 // 64KB Puffer
    }

private val serverLock = Any()  // Synchronisierungsobjekt

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connectionEstablished = MutableStateFlow(false)
    val connectionEstablished: StateFlow<Boolean> = _connectionEstablished.asStateFlow()

    // Flow für eingehende Daten (Quell-Adresse, Daten)
    private val _incomingData =
        MutableSharedFlow<Pair<String, ByteArray>>(replay = 0, extraBufferCapacity = 10)

    // Flow für Transferstatus-Updates (PaketId, Fortschritt in Prozent)
    private val _transferProgress =
        MutableSharedFlow<Triple<PaketId, String, Int>>(replay = 0, extraBufferCapacity = 10)

    // Verbindungsinformationen
    private var groupOwnerAddress: InetAddress? = null
    private var isGroupOwner = false

    // Server-Socket
    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)

    /**
     * Startet den WiFi Direct Service
     */
    fun startService() {
        Log.d(TAG, "Starting WiFi Direct service")
        isRunning.set(true)

        if (isGroupOwner) {
            startServer()
        }
    }

    /**
     * Stoppt den WiFi Direct Service
     */
    fun stopService() {
        Log.d(TAG, "Stopping WiFi Direct service")
        isRunning.set(false)

        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            Log.e(TAG, "Error closing server socket", e)
        }
    }

    /**
     * Wird aufgerufen, wenn eine WiFi Direct Verbindung hergestellt wurde
     */
    fun onConnectionEstablished(info: WifiP2pInfo) {
        groupOwnerAddress = info.groupOwnerAddress
        isGroupOwner = info.isGroupOwner

        Log.d(
            TAG,
            "Connection established - Group Owner: $isGroupOwner, Address: ${groupOwnerAddress?.hostAddress}"
        )
        Log.d(TAG, "Connection info - Group Formed: ${info.groupFormed}")


    isRunning.set(true)

        // Nur der Group Owner startet einen Server
        if (isGroupOwner) {
            Log.d(TAG, "Starting server as Group Owner")
            startServer()

            coroutineScope.launch {
                delay(1000)
                Log.d(
                    TAG,
                    "Server status after 1 second: running=${serverSocket != null && !serverSocket!!.isClosed}"
                )
            }
        } else {
            Log.d(TAG, "Client mode - will connect to server at ${groupOwnerAddress?.hostAddress}")
            // Als Client müssen wir keinen Server starten
        }

        // Signalisiere erfolgreich hergestellte Verbindung
        _connectionEstablished.value = true
    }

    /**
     * Startet einen Server, der auf eingehende Verbindungen wartet
     */
private fun startServer() {
    // Zuerst alten Server stoppen
    stopServer()

            isRunning.set(true)


    coroutineScope.launch {
        try {
            Log.d(TAG, "Starting server as ${if (isGroupOwner) "Group Owner" else "Client"}")
            val port = PORT

            // Erstelle und binde den Socket explizit
            serverSocket = withContext(Dispatchers.IO) {
                try {
                    val socket = ServerSocket()
                    socket.reuseAddress = true
                    socket.bind(InetSocketAddress("0.0.0.0", port))
                    Log.d(TAG, "Server socket successfully bound to 0.0.0.0:$port")
                    socket
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to bind on port $port: ${e.message}", e)
                    null
                }
            }

            if (serverSocket == null) {
                Log.e(TAG, "Failed to create server socket")
                return@launch
            }

            // Überprüfe sofort, ob der Socket erfolgreich erstellt wurde
            val localPort = serverSocket?.localPort ?: -1
            if (localPort <= 0) {
                Log.e(TAG, "Invalid server port: $localPort")
                return@launch
            }

            Log.d(TAG, "Server started on port $localPort. Waiting for clients...")

            // Teste den Server mit einer lokalen Verbindung
            testServerLocally(localPort)

                            if (!isRunning.get()) {
                    Log.w(TAG, "isRunning is false before entering server loop - fixing")
                    isRunning.set(true)
                }

            // WICHTIG: Definiere eine kontinuierliche Schleife, die auf Verbindungen wartet
            while (isRunning.get() && serverSocket != null && !serverSocket!!.isClosed) {
                Log.d(TAG, "Server loop iteration - ready to accept connections on port $localPort")

                try {
                    // WICHTIG: Statt blockierendem accept(), verwende ein Timeout-Muster
                    var clientSocket: Socket? = null
                    withTimeoutOrNull(5000) { // 5-Sekunden Timeout
                        Log.d(TAG, "Waiting for accept() to return...")
                        clientSocket = withContext(Dispatchers.IO) {
                            try {
                                serverSocket?.accept()
                            } catch (e: IOException) {
                                Log.e(TAG, "Error accepting connection: ${e.message}")
                                null
                            }
                        }
                    }

                    if (clientSocket != null) {
                        val clientAddress = clientSocket!!.inetAddress.hostAddress ?: "unknown"
                        Log.d(TAG, "Client connected from: $clientAddress")

                        // Starte einen separaten Coroutine für die Client-Verarbeitung
                        coroutineScope.launch {
                            handleClient(clientSocket!!)
                        }
                    } else {
                        Log.d(TAG, "No connection within timeout, continuing server loop")
                        delay(500) // Kurze Pause
                    }
                } catch (e: Exception) {
                    if (isRunning.get()) {
                        Log.e(TAG, "Error in server loop: ${e.message}")
                        delay(1000)
                    } else {
                        break
                    }
                }
            }

            Log.d(TAG, "Server loop ended. Running: ${isRunning.get()}, Socket closed: ${serverSocket?.isClosed ?: true}")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal server error: ${e.message}", e)
        }
    }
}

    private fun stopServer() {
        try {
            if (serverSocket != null && !serverSocket!!.isClosed) {
                Log.d(TAG, "Stopping server on port ${serverSocket?.localPort}")
                serverSocket?.close()
            }
            serverSocket = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}", e)
        }
    }

    private fun testServerLocally(port: Int) {
        coroutineScope.launch(Dispatchers.IO) {
            delay(1000) // Kurz warten, damit der Server initialisiert wird

            try {
                Log.d(TAG, "Testing local server connection to port $port")
                val testSocket = Socket()
                testSocket.connect(InetSocketAddress("127.0.0.1", port), 2000)
                Log.d(TAG, "Local server test successful: connected to 127.0.0.1:$port")
                testSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Local server test failed: ${e.message}", e)
                // Der lokale Test könnte aufgrund von Firewall/Routing-Problemen fehlschlagen
                // Protokolliere das, aber beende nicht den Server
            }
        }
    }

    /**
     * Sendet Daten an einen bestimmten Peer
     */
    /**
     * Sendet Daten an einen bestimmten Peer
     */
    suspend fun sendData(
        data: ByteArray,
        targetAddress: String,
        paketId: PaketId,
        port: Int = PORT
    ) {
        Log.d(
            TAG,
            "Preparing to send ${data.size} bytes to $targetAddress for paket ${paketId.value}"
        )
        Log.d(
            TAG,
            "Current connection state: isGroupOwner=$isGroupOwner, groupOwnerAddress=${groupOwnerAddress?.hostAddress}"
        )

        val actualTargetAddress: String

        if (isGroupOwner) {
            // Als Group Owner verwenden wir die übergebene Adresse (Client-Adresse)
            actualTargetAddress = targetAddress
            Log.d(TAG, "Group Owner sending to client at $actualTargetAddress")
        } else {
            // Als Client MÜSSEN wir die Group Owner Adresse verwenden
            actualTargetAddress = groupOwnerAddress?.hostAddress ?: targetAddress
            Log.d(TAG, "Client sending to Group Owner at $actualTargetAddress")
        }

        withContext(Dispatchers.IO) {
            var socket: Socket? = null
            var retryCount = 0
            val maxRetries = 10

            while (retryCount < maxRetries) {
                try {
                    Log.d(TAG, "Attempt ${retryCount + 1} to connect socket")

                    // Erstelle Socket und verbinde zum Ziel
                    socket = Socket()
                    socket.soTimeout = 15000 // Lese-Timeout setzen

                    // Korrigiere Adresse, wenn es sich um localhost handelt (für Tests)
                    val finalAddress = if (actualTargetAddress == "localhost") {
                        "127.0.0.1"
                    } else {
                        actualTargetAddress
                    }

                    val socketAddress = InetSocketAddress(finalAddress, port)

                    Log.d(TAG, "Connecting to $socketAddress with timeout 15000ms")
                    socket.connect(socketAddress, 15000) // 15 Sekunden Timeout

                    Log.d(TAG, "Socket connected successfully")

                    val outputStream = socket.getOutputStream()
                    val inputStream = socket.getInputStream()

                    // Sende zuerst die Größe der Nachricht (4 Bytes)
                    val size = data.size
                    val sizeBytes = byteArrayOf(
                        (size shr 24).toByte(),
                        (size shr 16).toByte(),
                        (size shr 8).toByte(),
                        size.toByte()
                    )
                    Log.d(TAG, "Sending size header: $size bytes")
                    outputStream.write(sizeBytes)
                    outputStream.flush()

                    // Sende die eigentlichen Daten in Chunks
                    var bytesSent = 0
                    val chunkSize = BUFFER_SIZE

                    // Emittiere 0% Fortschritt beim Start
                    _transferProgress.emit(Triple(paketId, finalAddress, 0))

                    while (bytesSent < data.size) {
                        val remaining = data.size - bytesSent
                        val bytesToSend = minOf(chunkSize, remaining)

                        outputStream.write(data, bytesSent, bytesToSend)
                        outputStream.flush()

                        bytesSent += bytesToSend

                        // Aktualisiere den Fortschritt
                        val progress = (bytesSent * 100) / data.size
                        Log.d(TAG, "Sent $bytesSent/${data.size} bytes ($progress%)")
                        _transferProgress.emit(Triple(paketId, finalAddress, progress))

                        // Kleine Pause, um UI-Updates zu ermöglichen
                        delay(50)
                    }

                    // Warte auf Bestätigung
                    Log.d(TAG, "Waiting for ACK from receiver")
                    val responseBuffer = ByteArray(3) // "ACK" ist 3 Bytes
                    val bytesRead = inputStream.read(responseBuffer)

                    if (bytesRead == 3 && String(responseBuffer) == "ACK") {
                        Log.d(TAG, "Transfer confirmed by receiver (ACK received)")
                        _transferProgress.emit(Triple(paketId, finalAddress, 100))
                    } else {
                        Log.e(
                            TAG,
                            "No proper acknowledgment received. Got: ${
                                String(
                                    responseBuffer,
                                    0,
                                    max(bytesRead, 0)
                                )
                            }"
                        )
                    }

                    // Erfolgreich gesendet, Schleife verlassen
                    break

                } catch (e: IOException) {
                    retryCount++
                    Log.e(TAG, "Connection attempt $retryCount failed: ${e.message}", e)

                    // Kurze Pause vor dem nächsten Versuch
                    if (retryCount < maxRetries) {
                        delay(1000)
                        Log.d(TAG, "Retrying connection after delay...")
                    } else {
                        // Bei maximalem Retry-Count den Fehler werfen
                        Log.e(TAG, "All retry attempts failed. Giving up.")
                        throw e
                    }
                } finally {
                    try {
                        socket?.close()
                    } catch (e: IOException) {
                        Log.e(TAG, "Error closing socket", e)
                    }
                }
            }
        }
    }

    /**
     * Beobachtet eingehende Daten
     * @return Flow von Paaren (Quell-Adresse, Daten)
     */
    fun observeIncomingData(): Flow<Pair<String, ByteArray>> {
        return _incomingData.asSharedFlow()
    }

    /**
     * Beobachtet den Fortschritt von Transfers
     * @return Flow von Triples (PaketId, Zieladresse, Fortschritt in %)
     */
    fun observeTransferProgress(): Flow<Triple<PaketId, String, Int>> {
        return _transferProgress.asSharedFlow()
    }

    // Füge diese Methode am Ende der Klasse hinzu
    /**
     * Nur für Tests: Emittiert einen Fortschrittswert
     */
    @VisibleForTesting
    suspend fun emitProgressForTest(paketId: PaketId, address: String, progress: Int) {
        _transferProgress.emit(Triple(paketId, address, progress))
    }

    /**
     * Verarbeitet einen verbundenen Client-Socket
     */
    private suspend fun handleClient(clientSocket: Socket) {
        try {
            val clientAddress = clientSocket.inetAddress.hostAddress ?: "unknown"
            Log.d(TAG, "Handling client from $clientAddress")

            val inputStream = clientSocket.getInputStream()
            val outputStream = clientSocket.getOutputStream()

            // Lese zuerst die Größe (4 Bytes)
            Log.d(TAG, "Reading size header from $clientAddress")
            val sizeBuffer = ByteArray(4)
            val bytesRead = inputStream.read(sizeBuffer)

            Log.d(TAG, "Read $bytesRead bytes for size header from $clientAddress")
            if (bytesRead != 4) {
                Log.e(TAG, "Failed to read size header, got only $bytesRead bytes")
                return
            }

            val messageSize = ((sizeBuffer[0].toInt() and 0xFF) shl 24) or
                    ((sizeBuffer[1].toInt() and 0xFF) shl 16) or
                    ((sizeBuffer[2].toInt() and 0xFF) shl 8) or
                    (sizeBuffer[3].toInt() and 0xFF)

            Log.d(TAG, "Receiving message of size: $messageSize bytes from $clientAddress")

            // Lese die eigentlichen Daten
            var totalBytesRead = 0
            val buffer = ByteArray(messageSize)

            while (totalBytesRead < messageSize) {
                val bytesRemaining = messageSize - totalBytesRead
                val chunkSize = minOf(BUFFER_SIZE, bytesRemaining)

                val chunk = withContext(Dispatchers.IO) {
                    inputStream.read(buffer, totalBytesRead, chunkSize)
                }

                if (chunk == -1) {
                    Log.e(TAG, "Connection closed before receiving complete data")
                    break
                }

                totalBytesRead += chunk
                val progress = (totalBytesRead * 100) / messageSize
                Log.d(TAG, "Received $totalBytesRead/$messageSize bytes ($progress%)")
            }

            if (totalBytesRead == messageSize) {
                Log.d(TAG, "Successfully received all data from $clientAddress")

                // Sende Bestätigung (ACK)
                outputStream.write("ACK".toByteArray())
                outputStream.flush()

                // Emittiere die empfangenen Daten
                _incomingData.emit(Pair(clientAddress, buffer))
            } else {
                Log.e(TAG, "Incomplete data transfer: $totalBytesRead/$messageSize bytes")
            }
            Log.d(TAG, "Sending ACK to $clientAddress")
            outputStream.write("ACK".toByteArray())
            outputStream.flush()
            Log.d(TAG, "ACK sent to $clientAddress")
        } catch (e: Exception) {
            Log.e(TAG, "Error in handleClient: ${e.message}", e)
        } finally {
            try {
                clientSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Error closing client socket: ${e.message}", e)
            }
        }
    }
}