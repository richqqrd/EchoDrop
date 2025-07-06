package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId

/**
 * Event das den Fortschritt eines automatischen Weiterleitungsversuchs beschreibt.
 */
data class ForwardEvent(
    val paketId: PaketId,
    val peerId: PeerId?,
    val stage: Stage,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class Stage {
        CONNECTING,
        TIMEOUT,
        SENT,
        FAILED
    }
} 