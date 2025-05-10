package com.example.echodrop.model.domainLayer.repository

import com.example.echodrop.model.domainLayer.model.Peer
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing peer-related operations
 */
interface PeerRepository {

    /**
     * Observes the list of known peers.
     *
     * @return A flow emitting a list of known peers.
     */
    fun observeKnownPeers(): Flow<List<Peer>>

    /**
     * Updates or inserts a peer into the repository.
     *
     * @param peer The peer to be updated or inserted.
     */
    suspend fun upsertPeer(peer: Peer)

    /**
     * Purges peers that have not been updated since the specified timestamp.
     *
     * @param olderThanUtc The timestamp (in UTC) used to determine stale peers.
     */
    suspend fun purgeStalePeers(olderThanUtc: Long)
}