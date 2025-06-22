package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId

/**
 * Interface für Callbacks zum Aktualisieren des Fortschritts von Transfers
 */
interface TransferProgressCallback {
    /**
     * Wird aufgerufen, wenn sich der Fortschritt eines Transfers ändert
     */
    suspend fun updateProgress(paketId: PaketId, peerId: PeerId, progressPct: Int)
}