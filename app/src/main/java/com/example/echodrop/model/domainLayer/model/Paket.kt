package com.example.echodrop.model.domainLayer.model

/**
 * Represents a package containing metadata and associated files.
 *
 * @property id The unique identifier of the package.
 * @property meta The metadata associated with the package.
 * @property sizeBytes The total size of the package in bytes.
 * @property sha256 The SHA-256 checksum of the package.
 * @property fileCount The total number of files in the package.
 * @property createdUtc The timestamp (in UTC) when the package was created.
 * @property files The list of files included in the package.
 */
data class Paket (
    val id: PaketId,
    val meta: PaketMeta,
    val sizeBytes: Long,
    val sha256: String,
    val fileCount: Int,
    val createdUtc: Long,
    val files: List<FileEntry>,
        val currentHopCount: Int = 0,
    val maxHopCount: Int? = null
) {
        fun canBeForwarded(): Boolean {
        if (maxHopCount == null) return true // Unbegrenzte Weitergabe
        return currentHopCount < maxHopCount
    }

    fun incrementHopCount(): Paket {
        return copy(currentHopCount = currentHopCount + 1)
    }
}