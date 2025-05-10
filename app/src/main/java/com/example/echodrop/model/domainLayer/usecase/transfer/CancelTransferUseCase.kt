package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import javax.inject.Inject

/**
 * Use case for canceling a transfer for a specific package and peer.
 *
 * @property repo The repository used to manage transfer operations.
 */
class CancelTransferUseCase @Inject constructor(
 private val repo: TransferRepository
) {
    /**
     * Invokes the use case to cancel a transfer.
     *
     * @param paketId The unique identifier of the package being transferred.
     * @param peerId The unique identifier of the peer involved in the transfer.
     */
    suspend operator fun invoke(paketId: PaketId, peerId: PeerId) = repo.cancel(paketId, peerId)
}