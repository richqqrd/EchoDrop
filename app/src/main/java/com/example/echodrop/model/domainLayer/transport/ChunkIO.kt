package com.example.echodrop.model.domainLayer.transport

import com.example.echodrop.model.domainLayer.model.PaketId
import java.io.File

/**
 * Kapselt das Aufteilen von Dateien in Chunks und das Zusammensetzen empfangener Chunks.
 */
interface ChunkIO {
    val chunkSize: Int

    /**
     * Zerteilt eine Datei in Chunks. Für jeden Chunk wird ein Paar aus [chunkId] und [data] geliefert.
     * Der [chunkIdPrefix] sollte bereits Paket- und Datei-Information enthalten.
     */
    fun splitFile(file: File, chunkIdPrefix: String): Sequence<Pair<String, ByteArray>>

    /**
     * Hängt einen empfangenen Chunk an die richtige Datei an.
     * Gibt true zurück, wenn das Schreiben erfolgreich war.
     */
    suspend fun appendChunk(paketId: PaketId, fileId: String, data: ByteArray): Boolean
} 