package com.example.echodrop.model.domainLayer.usecase.peer

import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import javax.inject.Inject

/**
 * Use case for creating or updating a `Peer` object.
 *
 * @property repo The repository used to manage `Peer` data.
 */
class UpsertPeerUseCase @Inject constructor(
    private val repo: PeerRepository
) {
    /**
     * Invokes the use case to create or update a `Peer`.
     *
     * @param peer The `Peer` object to be inserted or updated.
     */
    suspend operator fun invoke(peer: Peer) = repo.upsertPeer(peer)

}