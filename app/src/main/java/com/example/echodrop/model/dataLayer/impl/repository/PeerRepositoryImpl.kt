package com.example.echodrop.model.dataLayer.impl.repository

import android.util.Log
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.PeerDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PeerEntity
import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of the `PeerRepository` interface.
 *
 * @property peerDao The DAO used to access peer data in the database.
 */
class PeerRepositoryImpl @Inject constructor(
    private val peerDao: PeerDao
) : PeerRepository {
    override fun observeKnownPeers(): Flow<List<Peer>> {
        return peerDao.observeAll().map { entityList ->
            entityList.map {entity ->
                Peer(
                    id = PeerId(entity.peerId),
                    alias = entity.alias,
                    lastSeenUtc = entity.lastSeenUtc
                )
            }}
    }

    override suspend fun upsertPeer(peer: Peer) {
        val peerEntity = PeerEntity(
            peerId = peer.id.value,
            alias = peer.alias,
            lastSeenUtc = peer.lastSeenUtc
        )
        peerDao.upsert(peerEntity)
    }

    override suspend fun purgeStalePeers(olderThanUtc: Long) {
        val deleted = peerDao.purgeStale(olderThanUtc)
        Log.d("PeerRepositoryImpl", "purgeStalePeers deleted $deleted peer(s) older than $olderThanUtc")
    }
}