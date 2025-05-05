package com.example.echodrop.model.repository

import com.example.echodrop.domain.model.FileEntry
import com.example.echodrop.domain.model.PaketId

/**
 * Interface for managing file-related operations
 *
 */
interface FileRepository {

    /**
     * Retrieves all files associated with the given package ID.
     *
     * @param paketId The unique identifier of the package.
     * @return A list of file entries associated with the package.
     */
    suspend fun getFilesFor(paketId: PaketId): List<FileEntry>

    /**
     * Inserts a list of files for the given package ID into the repository.
     *
     * @param paketId The unique identifier of the package.
     * @param files The list of file entries to be inserted.
     */
    suspend fun insertAll(paketId: PaketId, files: List<FileEntry>)

    /**
     * Deletes all files associated with the given package ID from the repository.
     *
     * @param paketId The unique identifier of the package.
     */
    suspend fun deleteFor(paketId: PaketId)
}