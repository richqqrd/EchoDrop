package com.example.echodrop.domain.transport

sealed class IncomingFrame {
    data class ManifestFrame(val manifest: Manifest) : IncomingFrame()
    data class ChunkFrame(
        val paketId: String,
        val fileId: String,
        val chunkId: String,
        val offset: Long,
        val data: ByteArray
    ) : IncomingFrame()
}