package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import com.example.echodrop.model.domainLayer.transport.TransportManager
import javax.inject.Inject

/**
 * Use case for starting a transfer for a specific package and peer.
 *
 * @property repo The repository used to manage transfer operations.
 */
class StartTransferUseCase @Inject constructor(
    private val repo: TransferRepository,
    private val transportManager: TransportManager
) {
    suspend operator fun invoke(paketId: PaketId, peerId: PeerId) {
        repo.startTransfer(paketId, peerId, com.example.echodrop.model.domainLayer.model.TransferDirection.OUTGOING)
        
        try {
            transportManager.sendPaket(paketId, peerId)
        } catch (e: Exception) {
            repo.updateState(paketId, peerId, TransferState.FAILED)
            throw e
        }
    }
}