package com.example.echodrop.model.dataLayer.transport

import android.util.Log
import com.example.echodrop.model.domainLayer.repository.ConnectionAttemptRepository
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import com.example.echodrop.model.domainLayer.transport.MaintenanceScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Führt periodische Aufräumarbeiten in Hintergrund-Coroutines aus.
 */
@Singleton
class MaintenanceSchedulerImpl @Inject constructor(
    private val connectionAttemptRepository: ConnectionAttemptRepository,
    private val peerRepository: PeerRepository
) : MaintenanceScheduler {

    companion object {
        private const val TAG = "MaintenanceScheduler"
        private const val CLEANUP_TIMEOUT = 24 * 60 * 60 * 1000L // 24 h (ConnectionAttempt-Retention)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false

    override fun start() {
        if (started) return
        started = true

        // ConnectionAttempts aufräumen
        scope.launch {
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

        // Stale Peers bereinigen
        scope.launch {
            val interval = TimeUnit.HOURS.toMillis(6) // alle 6 h prüfen
            val peerCutoff = TimeUnit.DAYS.toMillis(7) // 7 Tage
            while (isActive) {
                try {
                    val deleted = peerRepository.purgeStalePeers(System.currentTimeMillis() - peerCutoff)
                    Log.d(TAG, "Peer cleanup removed ${'$'}deleted stale peer(s)")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during peer cleanup", e)
                }
                delay(interval)
            }
        }
    }

    override fun stop() {
        scope.cancel()
    }

    init {
        start() // auto-start beim Injizieren
    }
} 