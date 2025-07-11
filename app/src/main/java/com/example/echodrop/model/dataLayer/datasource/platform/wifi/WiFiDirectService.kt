package com.example.echodrop.model.dataLayer.datasource.platform.wifi

import android.content.Context
import android.net.wifi.p2p.WifiP2pInfo
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.transport.DirectSocketService
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
): DirectSocketService {
    companion object {
        private const val TAG = "WiFiDirectService"
        private const val PORT = 8988
        private const val BUFFER_SIZE = 64 * 1024
    }

private val serverLock = Any()

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connectionEstablished = MutableStateFlow(false)
    val connectionEstablished: StateFlow<Boolean> = _connectionEstablished.asStateFlow()

    private val _incomingData =
        MutableSharedFlow<Pair<String, ByteArray>>(replay = 0, extraBufferCapacity = 10)

    private val _transferProgress =
        MutableSharedFlow<Triple<PaketId, String, Int>>(replay = 0, extraBufferCapacity = 10)

    private var groupOwnerAddress: InetAddress? = null
    private var isGroupOwner = false

    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)


    /**
     * Startet den WiFi Direct Service
     */
    override fun startService() {
        Log.d(TAG, "Starting WiFi Direct service")
        isRunning.set(true)

        if (isGroupOwner) {
            startServer()
        }
    }

    /**
     * Stoppt den WiFi Direct Service
     */
    override fun stopService() {
        Log.d(TAG, "Stopping WiFi Direct service")
        isRunning.set(false)

        try {
            serverSocket?.close()
            serverSocket = null
        } catch (e: IOException) {
            Log.e(TAG, "Error closing server socket", e)
        }
    }

    override suspend fun sendData(
        data: ByteArray,
        targetAddress: String,
        paketId: PaketId
    ) {
        val port = PORT
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
            actualTargetAddress = targetAddress
            Log.d(TAG, "Group Owner sending to client at $actualTargetAddress")
        } else {
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

                    socket = Socket()
                    socket.soTimeout = 15000

                    val finalAddress = if (actualTargetAddress == "localhost") {
                        "127.0.0.1"
                    } else {
                        actualTargetAddress
                    }

                    val socketAddress = InetSocketAddress(finalAddress, port)

                    Log.d(TAG, "Connecting to $socketAddress with timeout 15000ms")
                    socket.connect(socketAddress, 15000) 

                    Log.d(TAG, "Socket connected successfully")

                    val outputStream = socket.getOutputStream()
                    val inputStream = socket.getInputStream()

                    val paketIdBytes = paketId.value.toByteArray()
                    val paketIdLength = paketIdBytes.size
                    outputStream.write(paketIdLength)
                    outputStream.write(paketIdBytes)
                    outputStream.flush()

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

                    var bytesSent = 0
                    val chunkSize = BUFFER_SIZE

                    _transferProgress.emit(Triple(paketId, finalAddress, 0))

                    while (bytesSent < data.size) {
                        val remaining = data.size - bytesSent
                        val bytesToSend = minOf(chunkSize, remaining)

                        outputStream.write(data, bytesSent, bytesToSend)
                        outputStream.flush()

                        bytesSent += bytesToSend

                        val progress = (bytesSent * 100) / data.size
                        Log.d(TAG, "Sent $bytesSent/${data.size} bytes ($progress%)")
                        _transferProgress.emit(Triple(paketId, finalAddress, progress))

                        delay(50)
                    }

                    Log.d(TAG, "Waiting for ACK from receiver")
                    val responseBuffer = ByteArray(3) 
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

                    break

                } catch (e: IOException) {
                    retryCount++
                    Log.e(TAG, "Connection attempt $retryCount failed: ${e.message}", e)

                    if (retryCount < maxRetries) {
                        delay(1000)
                        Log.d(TAG, "Retrying connection after delay...")
                    } else {
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
    override fun observeIncomingData(): Flow<Pair<String, ByteArray>> {
        return _incomingData.asSharedFlow()
    }

    /**
     * Beobachtet den Fortschritt von Transfers
     * @return Flow von Triples (PaketId, Zieladresse, Fortschritt in %)
     */
    override fun observeTransferProgress(): Flow<Triple<PaketId, String, Int>> {
        return _transferProgress.asSharedFlow()
    }

    override fun onConnectionEstablished(info: WifiP2pInfo) {
        groupOwnerAddress = info.groupOwnerAddress
        isGroupOwner = info.isGroupOwner

        Log.d(
            TAG,
            "Connection established - Group Owner: $isGroupOwner, Address: ${groupOwnerAddress?.hostAddress}"
        )
        Log.d(TAG, "Connection info - Group Formed: ${info.groupFormed}")

        isRunning.set(true)

        if (isGroupOwner) {
            Log.d(TAG, "Starting server as Group Owner")
            startServer()

            coroutineScope.launch {
                delay(1000)
                Log.d(
                    TAG,
                    "Server status after 1 second: running=${serverSocket != null && serverSocket?.isClosed == false}"
                )
            }
        } else {
            Log.d(TAG, "Client mode - will connect to server at ${groupOwnerAddress?.hostAddress}")
        }

        _connectionEstablished.value = true
    }

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

            val paketIdLength = inputStream.read()
            if (paketIdLength <= 0) {
                Log.e(TAG, "Invalid paketId length: $paketIdLength")
                return
            }
            val paketIdBytes = ByteArray(paketIdLength)
            val readBytes = inputStream.read(paketIdBytes)
            if (readBytes != paketIdLength) {
                Log.e(TAG, "Could not read complete paketId, expected $paketIdLength bytes but got $readBytes")
                return
            }
            val paketId = PaketId(String(paketIdBytes))
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

                _transferProgress.emit(Triple(paketId, clientAddress, progress))

            }

            if (totalBytesRead == messageSize) {
                Log.d(TAG, "Successfully received all data from $clientAddress")

                outputStream.write("ACK".toByteArray())
                outputStream.flush()

                _incomingData.emit(Pair(clientAddress, buffer))
            } else {
                Log.e(TAG, "Incomplete data transfer: $totalBytesRead/$messageSize bytes")
            }
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

    /**
     * Startet einen Server, der eingehende Socket-Verbindungen akzeptiert, falls dieses Gerät
     * Group-Owner ist. Läuft in einer IO-Coroutine und ruft für jeden Client `handleClient()` auf.
     */
    private fun startServer() {
        stopServer()

        isRunning.set(true)

        coroutineScope.launch {
            try {
                serverSocket = withContext(Dispatchers.IO) {
                    ServerSocket().apply {
                        reuseAddress = true
                        bind(InetSocketAddress("0.0.0.0", PORT))
                    }
                }

                Log.d(TAG, "Server started on port ${serverSocket?.localPort}")

                while (isRunning.get() && serverSocket?.isClosed == false) {
                    val client = withContext(Dispatchers.IO) {
                        try {
                            serverSocket?.accept()
                        } catch (e: IOException) {
                            Log.e(TAG, "Accept failed: ${e.message}")
                            null
                        }
                    }

                    if (client != null) {
                        Log.d(TAG, "Client connected from ${client.inetAddress?.hostAddress}")
                        launch { handleClient(client) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Server loop error", e)
            } finally {
                stopServer()
            }
        }
    }

    /**
     * Beendet den ServerSocket, falls aktiv.
     */
    private fun stopServer() {
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error stopping server", e)
        } finally {
            serverSocket = null
        }
    }
}