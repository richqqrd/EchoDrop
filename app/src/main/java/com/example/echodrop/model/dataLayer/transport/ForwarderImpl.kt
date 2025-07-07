package com.example.echodrop.model.dataLayer.transport

import android.util.Log
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.repository.ConnectionAttemptRepository
import com.example.echodrop.model.domainLayer.transport.DeviceDiscovery
import com.example.echodrop.model.domainLayer.transport.ForwardEvent
import com.example.echodrop.model.domainLayer.transport.Forwarder
import com.example.echodrop.model.domainLayer.transport.TransportManager
import com.example.echodrop.model.domainLayer.usecase.paket.SavePaketUseCase
import dagger.Lazy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForwarderImpl @Inject constructor(
    private val deviceDiscovery: DeviceDiscovery,
    private val savePaketUseCase: SavePaketUseCase,
    private val connectionAttemptRepository: ConnectionAttemptRepository,
    private val transportManagerLazy: Lazy<TransportManager>,
    private val forwardFlow: MutableSharedFlow<ForwardEvent>
) : Forwarder {

    companion object {
        private const val TAG = "ForwarderImpl"
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

    override val events: SharedFlow<ForwardEvent> = forwardFlow

    override suspend fun autoForward(paket: Paket, excludeAddress: String?) {
        try {
            Log.d(TAG, "[Forward] Starte automatische Weiterleitung für Paket ${paket.id.value}")

            // 1. Starte Discovery (falls nicht bereits aktiv)
            deviceDiscovery.startDiscovery()

            delay(5_000) // Gib der Peer-Discovery etwas Zeit

            // 2. Geräte ermitteln & filtern
            val allDevices = deviceDiscovery.discoveredDevices.value
            val candidates = allDevices
                .filterNot { DEVICE_BLACKLIST.contains(it.deviceAddress) }
                .filterNot { excludeAddress != null && it.deviceAddress == excludeAddress }
                .shuffled()
                .take(3) // max. 3 Versuche pro Weiterleitung

            if (candidates.isEmpty()) {
                Log.d(TAG, "[Forward] Keine geeigneten Peers gefunden – Abbruch")
                return
            }

            // 4. Iteriere über Kandidaten
            for (device in candidates) {
                val devAddr = device.deviceAddress

                // Falls wir noch in einer bestehenden Gruppe sind, zuerst trennen und warten
                if (deviceDiscovery.connectionInfo.value?.groupFormed == true) {
                    Log.d(TAG, "[Forward] Bestehende Gruppe erkannt – trenne, bevor ich zu $devAddr verbinde")
                    deviceDiscovery.disconnectFromCurrentGroup()
                    val leftGroup = waitForNoGroup()
                    if (!leftGroup) {
                        Log.d(TAG, "[Forward] Konnte Gruppe nicht rechtzeitig verlassen – überspringe $devAddr")
                        continue
                    }

                    // Neue Peer-Discovery starten, damit das zuvor verlassene Gerät uns wieder sieht
                    Log.d(TAG, "[Forward] Starte erneute Peer-Discovery nach Gruppen-Trennung")
                    deviceDiscovery.startDiscovery()
                    delay(4_000)
                }

                // Retry-Limit prüfen
                if (isDeviceAlreadyTried(devAddr, paket.id)) {
                    Log.d(TAG, "[Forward] $devAddr bereits $MAX_ATTEMPTS× erfolglos – überspringe")
                    continue
                }

                Log.d(TAG, "[Forward] Verbinde zu ${device.deviceName} ($devAddr)")
                forwardFlow.emit(
                    ForwardEvent(
                        paketId = paket.id,
                        peerId = PeerId("direct-$devAddr"),
                        stage = ForwardEvent.Stage.CONNECTING,
                        message = "Verbinde zu ${device.deviceName}"
                    )
                )

                try {
                    // Verbindungsaufbau
                    deviceDiscovery.connectToDevice(devAddr)

                    // Warte, bis die Gruppe steht
                    val connected = waitForGroup()
                    if (!connected) {
                        Log.d(TAG, "[Forward] Timeout – Gruppe mit $devAddr nicht aufgebaut")
                        forwardFlow.emit(
                            ForwardEvent(paket.id, PeerId("direct-$devAddr"), ForwardEvent.Stage.TIMEOUT, "Timeout bei Verbindung")
                        )
                        connectionAttemptRepository.trackAttempt(devAddr, paket.id, false)
                        continue
                    }

                    val peerId = PeerId("direct-$devAddr")

                    // Paket senden – über TransportManager
                    transportManagerLazy.get().sendPaket(paket.id, peerId)
                    forwardFlow.emit(
                        ForwardEvent(paket.id, peerId, ForwardEvent.Stage.SENT, "Paket gesendet")
                    )

                    connectionAttemptRepository.trackAttempt(devAddr, paket.id, true)

                    Log.d(TAG, "[Forward] Paket ${paket.id.value} erfolgreich an ${device.deviceName} weitergeleitet")

                    // Nachgelagertes Disconnect
                    deviceDiscovery.disconnectFromCurrentGroup()
                } catch (e: Exception) {
                    Log.e(TAG, "[Forward] Fehler beim Weiterleiten an ${device.deviceName}: ${e.message}")
                    forwardFlow.emit(
                        ForwardEvent(paket.id, PeerId("direct-$devAddr"), ForwardEvent.Stage.FAILED, e.message ?: "Unbekannter Fehler")
                    )
                    connectionAttemptRepository.trackAttempt(devAddr, paket.id, false)
                    deviceDiscovery.disconnectFromCurrentGroup()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[Forward] Unerwarteter Fehler: ${e.message}", e)
        } finally {
            // Discovery beenden, damit UI-State sauber bleibt
            deviceDiscovery.stopDiscovery()
        }
    }

    private suspend fun waitForGroup(maxWaitMs: Long = 20_000): Boolean {
        val start = System.currentTimeMillis()
        while (deviceDiscovery.connectionInfo.value?.groupFormed != true) {
            if (System.currentTimeMillis() - start > maxWaitMs) return false
            delay(500)
        }
        return true
    }

    private suspend fun waitForNoGroup(maxWaitMs: Long = 15_000): Boolean {
        val start = System.currentTimeMillis()
        while (deviceDiscovery.connectionInfo.value?.groupFormed == true) {
            if (System.currentTimeMillis() - start > maxWaitMs) return false
            delay(500)
        }
        return true
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