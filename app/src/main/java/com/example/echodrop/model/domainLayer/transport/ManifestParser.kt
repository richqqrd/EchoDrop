package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PeerId

/**
 * Gegenstück zum [ManifestBuilder].
 * Wandelt einen empfangenen Manifest-JSON-String in ein [Paket]-Objekt um.
 */
interface ManifestParser {
    /**
     * @param paketId      ID des Pakets
     * @param manifestJson Manifest-Inhalt als JSON-String (ohne Präfixe)
     * @param senderPeer   Peer, von dem das Manifest stammt
     * @return Das resultierende [Paket]
     */
    fun parse(paketId: String, manifestJson: String, senderPeer: PeerId): Paket
} 