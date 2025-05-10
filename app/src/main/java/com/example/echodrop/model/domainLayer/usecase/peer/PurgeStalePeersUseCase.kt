package com.example.echodrop.model.domainLayer.usecase.peer

import com.example.echodrop.model.domainLayer.repository.PeerRepository
import javax.inject.Inject

/**
 * Use case for purging stale `Peer` entries.
 *
 * @property repo The repository used to manage `Peer` data.
 */
class PurgeStalePeersUseCase @Inject constructor(
    private val repo: PeerRepository
) {
    /**
     * Invokes the use case to purge stale `Peer` entries.
     *
     * @param olderThanUtc The UTC timestamp; `Peer` entries older than this will be purged.
     */
    suspend operator fun invoke(olderThanUtc: Long) = repo.purgeStalePeers(olderThanUtc)
}