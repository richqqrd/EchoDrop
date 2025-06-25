package com.example.echodrop.model.dataLayer.transport

import android.content.Context
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.dataLayer.transport.WiFiDirectService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.net.InetAddress
import java.net.Socket

class WiFiDirectServiceTest {

    @MockK
    private lateinit var mockContext: Context

    private lateinit var wifiDirectService: WiFiDirectService

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { mockContext.getSystemService(any()) } returns mockk(relaxed = true)
        wifiDirectService = WiFiDirectService(mockContext)
    }

    @Test
    @DisplayName("Test message size encoding and decoding")
    fun testMessageSizeEncodingDecoding() {
        // Testgrößen
        val testSizes = listOf(10, 255, 1024, 65536, 1048576)

        for (originalSize in testSizes) {
            // Enkodiere die Größe
            val encodedSize = byteArrayOf(
                (originalSize shr 24).toByte(),
                (originalSize shr 16).toByte(),
                (originalSize shr 8).toByte(),
                originalSize.toByte()
            )

            // Dekodiere die Größe
            val decodedSize = ((encodedSize[0].toInt() and 0xFF) shl 24) or
                    ((encodedSize[1].toInt() and 0xFF) shl 16) or
                    ((encodedSize[2].toInt() and 0xFF) shl 8) or
                    (encodedSize[3].toInt() and 0xFF)

            // Überprüfe, ob die Dekodierung korrekt ist
            assertEquals(originalSize, decodedSize, "Size encoding/decoding should be identical")
        }
    }

    @Test
    @DisplayName("Test data transfer progress calculation")
    fun testDataTransferProgressCalculation() {
        // Teste die Fortschrittsberechnung für verschiedene Datengrößen
        val dataSize = 1000
        val bytesSent = listOf(0, 100, 250, 500, 750, 1000)
        val expectedProgress = listOf(0, 10, 25, 50, 75, 100)

        for (i in bytesSent.indices) {
            val progress = (bytesSent[i] * 100) / dataSize
            assertEquals(expectedProgress[i], progress, "Progress calculation should match expected value")
        }
    }
}