package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.dataLayer.transport.WiFiDirectService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Integrationstest für WiFiDirectService
 */
@RunWith(AndroidJUnit4::class)
class WiFiDirectIntegrationTest {

    private lateinit var context: Context
    private lateinit var wifiDirectService: WiFiDirectService

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        wifiDirectService = WiFiDirectService(context)
    }

    @Test
    fun testServerSocketCreation() {
        var serverSocket: ServerSocket? = null
        try {
            serverSocket = ServerSocket()
            serverSocket.reuseAddress = true
            val safePort = 9876 
            serverSocket.bind(InetSocketAddress(safePort))

            val boundPort = serverSocket.localPort
            assertTrue("Server socket should be bound to a port", boundPort > 0)
            assertEquals("Server socket should be bound to requested port", safePort, boundPort)
        } finally {
            serverSocket?.close()
        }
    }

    @Test
    fun testDataTransferBetweenSockets() {
        val testData = "Hello EchoDrop Test".toByteArray()
        val testPaketId = PaketId("test-paket-123")
        val serverReady = CountDownLatch(1)
        val transferComplete = CountDownLatch(1)
        var receivedData: ByteArray? = null
        var serverPort = -1

        val serverThread = thread {
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(0) 
                serverPort = serverSocket.localPort
                println("Server started on port $serverPort")

                serverReady.countDown()

                val clientSocket = serverSocket.accept()
                println("Client connected to server")

                val sizeBuffer = ByteArray(4)
                val sizeBytesRead = clientSocket.getInputStream().read(sizeBuffer)
                println("Read $sizeBytesRead size bytes")

                val messageSize = ((sizeBuffer[0].toInt() and 0xFF) shl 24) or
                        ((sizeBuffer[1].toInt() and 0xFF) shl 16) or
                        ((sizeBuffer[2].toInt() and 0xFF) shl 8) or
                        (sizeBuffer[3].toInt() and 0xFF)
                println("Message size: $messageSize bytes")

                receivedData = ByteArray(messageSize)
                var totalBytesRead = 0

                while (totalBytesRead < messageSize) {
                    val bytesRead = clientSocket.getInputStream().read(
                        receivedData!!,
                        totalBytesRead,
                        messageSize - totalBytesRead
                    )
                    if (bytesRead == -1) break
                    totalBytesRead += bytesRead
                    println("Read $totalBytesRead/$messageSize bytes")
                }

                clientSocket.getOutputStream().write("ACK".toByteArray())
                clientSocket.getOutputStream().flush()
                println("Sent ACK to client")

                transferComplete.countDown()

                clientSocket.close()
            } catch (e: Exception) {
                println("Server error: ${e.message}")
                e.printStackTrace()
            } finally {
                serverSocket?.close()
            }
        }

        assertTrue("Server should start within timeout", serverReady.await(5, TimeUnit.SECONDS))
        println("Server is ready on port $serverPort")

        runBlocking {
            try {
                println("Client connecting to localhost:$serverPort")
                val socket = Socket()
                socket.connect(InetSocketAddress("localhost", serverPort), 5000)
                println("Client connected")

                val size = testData.size
                val sizeBytes = byteArrayOf(
                    (size shr 24).toByte(),
                    (size shr 16).toByte(),
                    (size shr 8).toByte(),
                    size.toByte()
                )
                socket.getOutputStream().write(sizeBytes)
                socket.getOutputStream().flush()
                println("Client sent size: $size bytes")

                socket.getOutputStream().write(testData)
                socket.getOutputStream().flush()
                println("Client sent data: ${String(testData)}")

                val response = ByteArray(3)  
                val responseSize = socket.getInputStream().read(response)
                println("Client received response: ${String(response, 0, responseSize)}")

                socket.close()
            } catch (e: Exception) {
                println("Client error: ${e.message}")
                e.printStackTrace()
            }
        }

        val completed = transferComplete.await(10, TimeUnit.SECONDS)

        assertTrue("Data transfer should complete", completed)
        assertTrue("Data should be received", receivedData != null)
        assertEquals("Received data should match sent data",
            String(testData), receivedData?.let { String(it) })
    }

    @Test
    fun testTransferProgressFlow() = runBlocking {
        val testPaketId = PaketId("test-paket-456")
        val testAddress = "192.168.49.1"
        val testProgress = listOf(0, 25, 50, 75, 100)

        val collectorReadyLatch = CountDownLatch(1)
        val allValuesCollectedLatch = CountDownLatch(1)

        val progressValues = mutableListOf<Int>()

        val job = thread {
            runBlocking {
                collectorReadyLatch.countDown()

                try {
                    withTimeout(10000) { 
                        wifiDirectService.observeTransferProgress()
                            .take(5)
                            .collect { (_, _, progress) ->
                                progressValues.add(progress)

                                if (progressValues.size == 5) {
                                    allValuesCollectedLatch.countDown()
                                }
                            }
                    }
                } catch (e: Exception) {
                    println("Error in collector: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        assertTrue("Collector should start", collectorReadyLatch.await(2, TimeUnit.SECONDS))

        Thread.sleep(500)

        for (progress in testProgress) {
            wifiDirectService.emitProgressForTest(testPaketId, testAddress, progress)
            Thread.sleep(200) 
        }

        assertTrue("All values should be collected", allValuesCollectedLatch.await(5, TimeUnit.SECONDS))

        assertEquals("Should collect all progress updates", testProgress.size, progressValues.size)
        assertEquals("Progress values should match", testProgress, progressValues)
    }
}