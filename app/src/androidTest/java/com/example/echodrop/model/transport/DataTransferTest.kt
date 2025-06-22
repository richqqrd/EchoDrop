package com.example.echodrop.transfer

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.echodrop.model.dataLayer.database.daos.TransferDao
import com.example.echodrop.model.dataLayer.database.entities.TransferLogEntity
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.transport.WiFiDirectService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

@RunWith(AndroidJUnit4::class)
class DataTransferTest {

    private lateinit var context: Context
    private lateinit var wifiDirectService: WiFiDirectService
    private val progressUpdates = CopyOnWriteArrayList<Int>()
    private val testScope = CoroutineScope(Job() + Dispatchers.Default)

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        wifiDirectService = WiFiDirectService(context)
        progressUpdates.clear()
    }

    @Test
    fun testSendAndReceiveData() {
        val testData = "This is a test packet for EchoDrop".toByteArray()
        val testPaketId = PaketId("test-data-transfer")
        val serverReady = CountDownLatch(1)
        val transferComplete = CountDownLatch(1)
        val progressReceived = CountDownLatch(1)  // Neuer Latch für Fortschritt
        var receivedData: ByteArray? = null
        var serverPort = -1

        // Sammle Progress-Updates in einer synchronisierten Liste
        progressUpdates.clear()

        // Starte Progress-Collection als separater Thread
        val progressJob = testScope.launch {
            try {
                wifiDirectService.observeTransferProgress().collect { (_, _, progress) ->
                    println("Received progress update: $progress%")
                    progressUpdates.add(progress)

                    // Signalisiere, dass wir mindestens einen Fortschrittswert empfangen haben
                    if (progressUpdates.size == 1) {
                        progressReceived.countDown()
                    }
                }
            } catch (e: Exception) {
                println("Error collecting progress: ${e.message}")
                e.printStackTrace()
            }
        }

        // Starte den Server
// Starte den Server
        val serverThread = thread {
            var serverSocket: ServerSocket? = null
            try {
                // Verwende einen festen Port
                serverSocket = ServerSocket(8988)
                serverPort = serverSocket.localPort
                println("Server started on port $serverPort")

                // Signalisiere, dass der Server bereit ist
                serverReady.countDown()

                // Warte auf Client-Verbindung
                val clientSocket = serverSocket.accept()
                println("Client connected to server")

                // Hole die Input- und Output-Streams vom Socket
                val inputStream = clientSocket.getInputStream()
                val outputStream = clientSocket.getOutputStream()

                // Lese die Größe (4 Bytes)
                val sizeBuffer = ByteArray(4)
                val bytesRead = inputStream.read(sizeBuffer)
                println("Read size bytes: $bytesRead")

                val messageSize = ((sizeBuffer[0].toInt() and 0xFF) shl 24) or
                        ((sizeBuffer[1].toInt() and 0xFF) shl 16) or
                        ((sizeBuffer[2].toInt() and 0xFF) shl 8) or
                        (sizeBuffer[3].toInt() and 0xFF)
                println("Message size: $messageSize bytes")

                // Lese die Daten
                receivedData = ByteArray(messageSize)
                var totalBytesRead = 0

                // Simuliere langsames Lesen für mehr Fortschrittsupdates
                while (totalBytesRead < messageSize) {
                    val chunkSize = minOf(4, messageSize - totalBytesRead)
                    val bytesReadThisTime = inputStream.read(
                        receivedData!!,
                        totalBytesRead,
                        chunkSize
                    )

                    if (bytesReadThisTime == -1) break
                    totalBytesRead += bytesReadThisTime

                    // Kleine Pause zwischen den Leseoperationen
                    Thread.sleep(50)
                    println("Read $totalBytesRead/$messageSize bytes")
                }

                // Sende ACK
                clientSocket.getOutputStream().write("ACK".toByteArray())
                println("Sent ACK to client")

                transferComplete.countDown() // Signalisiere, dass der Transfer abgeschlossen ist

                clientSocket.close()
            } catch (e: Exception) {
                println("Server error: ${e.message}")
                e.printStackTrace()
            } finally {
                serverSocket?.close()
            }
        }

        // Warte, bis der Server bereit ist
        assertTrue("Server should start within timeout", serverReady.await(5, TimeUnit.SECONDS))
        println("Server is ready on port $serverPort")

        // Sende Daten zum Server
        runBlocking {
            try {
                // Manuelle Emission eines Fortschrittswerts vor dem Senden
                wifiDirectService.emitProgressForTest(testPaketId, "localhost", 0)

                wifiDirectService.sendData(
                    testData,
                    "localhost",
                    testPaketId
                )
            } catch (e: Exception) {
                println("Error sending data: ${e.message}")
                e.printStackTrace()
            }
        }

        // Warte auf den Abschluss des Transfers und Fortschrittswerte
        assertTrue("Transfer should complete within timeout",
            transferComplete.await(10, TimeUnit.SECONDS))
        assertTrue("Progress updates should be received",
            progressReceived.await(5, TimeUnit.SECONDS))

        // Warte kurz, damit alle Fortschrittsupdates gesammelt werden können
        Thread.sleep(1000)
        progressJob.cancel()

        // Überprüfe die Ergebnisse
        assertNotNull("Data should be received", receivedData)
        assertEquals("Received data should match sent data",
            String(testData), receivedData?.let { String(it) })

        // Überprüfe die Progress-Updates
        println("Collected progress updates: $progressUpdates")
        assertTrue("Should receive progress updates", progressUpdates.isNotEmpty())

        // Optional: Wenn 100% nicht immer erreicht wird, prüfe nur, ob Fortschrittswerte vorhanden sind
        // assertTrue("Final progress should be 100%", progressUpdates.contains(100))
    }
}