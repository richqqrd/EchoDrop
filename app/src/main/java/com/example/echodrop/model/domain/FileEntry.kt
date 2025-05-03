package com.example.echodrop.model.domain

/**
 * data class for a file entry in a package
 */
data class FileEntry(
    val path: String,
    val mime: String,
    val sizeBytes: Long,
    val orderIdx: Int
)
