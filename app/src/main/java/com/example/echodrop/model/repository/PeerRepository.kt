package com.example.echodrop.model.repository

import com.example.echodrop.model.domain.Peer
import kotlinx.coroutines.flow.Flow

interface PeerRepository {

    fun observeKnownPeers(): Flow<List<Peer>>

    suspend fun upsertPeer(peer: Peer)

    suspend fun purgeStalePeers(olderThanUtc: Long)
}