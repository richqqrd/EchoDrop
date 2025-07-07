package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.Paket
import kotlinx.coroutines.flow.SharedFlow

/**
 * Verantwortlich für das automatische Weiterleiten (Forwarding) von Paketen an weitere Peers.
 */
interface Forwarder {
    val events: SharedFlow<ForwardEvent>

    suspend fun autoForward(paket: Paket, excludeAddress: String? = null)
} 