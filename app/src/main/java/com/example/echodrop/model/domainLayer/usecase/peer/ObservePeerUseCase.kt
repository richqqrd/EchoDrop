package com.example.echodrop.model.domainLayer.usecase.peer

import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow


/**
 * Use case for observing the list of known `Peer` objects.
 *
 * @property repo The repository used to manage `Peer` data.
 */
class ObservePeerUseCase @Inject constructor(
    private val repo: PeerRepository
) {
    /**
     * Invokes the use case to observe the list of known `Peer` objects.
     *
     * @return A flow emitting a list of `Peer` objects.
     */
    operator fun invoke(): Flow<List<Peer>> = repo.observeKnownPeers()
}