package com.example.echodrop.model.domain

/**
 * Represents a file entry within a package.
 *
 * @property path The file path of the entry.
 * @property mime The MIME type of the file.
 * @property sizeBytes The size of the file in bytes.
 * @property orderIdx The order index of the file within the package.
 */
data class FileEntry(
    val path: String,
    val mime: String,
    val sizeBytes: Long,
    val orderIdx: Int
)
