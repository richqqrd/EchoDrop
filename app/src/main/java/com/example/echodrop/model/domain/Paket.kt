package com.example.echodrop.model.domain

/**
 * data class for a whole package (meta and files)
 */
data class Paket (
    val id: PaketId,
    val meta: PaketMeta,
    val sizeBytes: Long,
    val sha256: String,
    val fileCount: Int,
    val createdUtc: Long,
    val files: List<FileEntry>
)