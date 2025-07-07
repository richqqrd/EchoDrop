package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.PaketId

/**
 * Erstellt das JSON-Manifest zu einem Paket, das an andere Peers gesendet wird.
 */
interface ManifestBuilder {
    suspend fun build(paketId: PaketId): String
} 