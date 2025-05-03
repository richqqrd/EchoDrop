package com.example.echodrop.model.repository

import com.example.echodrop.model.domain.FileEntry
import com.example.echodrop.model.domain.Paket
import com.example.echodrop.model.domain.PaketId
import com.example.echodrop.model.domain.PaketMeta
import kotlinx.coroutines.flow.Flow

/**
 * Interface for a repository of packages (meta and files).
 */
interface PaketRepository {

    /**
     * Observe changes to the inbox
     */
    fun observeInbox(): Flow<List<Paket>>

    /**
     * Get a single package by its ID
     */
    suspend fun getPaket(id: PaketId): Paket?

    /**
     * Insert a package into the repository
     */
    suspend fun insert(meta: PaketMeta, files: List<FileEntry>): PaketId

    /**
     * Update a package's metadata
     */
    suspend fun updateMeta(id: PaketId, ttlSeconds: Int, priority: Int)

    /**
     * Delete a package from the repository
     */
    suspend fun delete(id: PaketId)

    /**
     * Purge expired packages from the repository
     */
    suspend fun purgeExpire(nowUtc: Long): Int


}