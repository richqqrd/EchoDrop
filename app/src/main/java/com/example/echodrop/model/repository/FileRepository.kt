package com.example.echodrop.model.repository

import com.example.echodrop.model.domain.FileEntry
import com.example.echodrop.model.domain.PaketId

/**
 * Interface for a repository of files
 */
interface FileRepository {

    /**
     * Get all files for a package
     */
    suspend fun getFilesFor(paketId: PaketId): List<FileEntry>

    /**
     * Insert all files for a package
     */
    suspend fun insertAll(paketId: PaketId, files: List<FileEntry>)

    /**
     * Delete all files for a package
     */
    suspend fun deleteFor(paketId: PaketId)
}