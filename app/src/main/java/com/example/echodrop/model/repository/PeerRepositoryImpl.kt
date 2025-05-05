package com.example.echodrop.model.repository

import com.example.echodrop.model.database.daos.PeerDao
import com.example.echodrop.domain.model.Peer
import com.example.echodrop.domain.model.PeerId
import com.example.echodrop.model.database.entities.PeerEntity
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
) : PeerRepository{
    override fun observeKnownPeers(): Flow<List<Peer>> {
        return peerDao.observeAll().map {entityList ->
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
        peerDao.purgeStale(olderThanUtc)    }
}