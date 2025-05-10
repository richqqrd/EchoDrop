package com.example.echodrop.model.domainLayer.repository

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing package-related operations
 *
 */
interface PaketRepository {

    /**
     * Observes changes to the inbox.
     *
     * @return A flow emitting a list of packages in the inbox.
     */
    fun observeInbox(): Flow<List<Paket>>

    /**
     * Retrieves a single package by its ID.
     *
     * @param id The unique identifier of the package.
     * @return The package with the specified ID, or null if not found.
     */
    suspend fun getPaket(id: PaketId): Paket?

    /**
     * Inserts a package into the repository.
     *
     * @param meta The metadata of the package.
     * @param files The list of files associated with the package.
     * @return The unique identifier of the newly inserted package.
     */
    suspend fun insert(meta: PaketMeta, files: List<FileEntry>): PaketId

    /**
     * Updates the metadata of a package.
     *
     * @param id The unique identifier of the package.
     * @param ttlSeconds The new time-to-live (TTL) value in seconds.
     * @param priority The new priority level of the package.
     */
    suspend fun updateMeta(id: PaketId, ttlSeconds: Int, priority: Int)

    /**
     * Deletes a package from the repository.
     *
     * @param id The unique identifier of the package to be deleted.
     */
    suspend fun delete(id: PaketId)

    /**
     * Purges expired packages from the repository.
     *
     * @param nowUtc The current timestamp (in UTC) used to determine expiration.
     * @return The number of packages that were purged.
     */
    suspend fun purgeExpire(nowUtc: Long): Int


}