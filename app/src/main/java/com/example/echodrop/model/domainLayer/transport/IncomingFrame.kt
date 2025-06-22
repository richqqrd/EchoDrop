package com.example.echodrop.model.domainLayer.transport

sealed class IncomingFrame {
    data class Manifest(
        val sourceAddress: String,
        val paketId: String,
        val manifestJson: String
    ) : IncomingFrame()

    data class Chunk(
        val sourceAddress: String,
        val chunkId: String,
        val data: ByteArray
    ) : IncomingFrame() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Chunk

            if (sourceAddress != other.sourceAddress) return false
            if (chunkId != other.chunkId) return false
            if (!data.contentEquals(other.data)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = sourceAddress.hashCode()
            result = 31 * result + chunkId.hashCode()
            result = 31 * result + data.contentHashCode()
            return result
        }
    }
}