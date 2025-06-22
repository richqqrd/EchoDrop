package com.example.echodrop.model.domainLayer.usecase.transfer

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import javax.inject.Inject

/**
 * UseCase zum Aktualisieren des Fortschritts eines Transfers
 */
class UpdateTransferProgressUseCase @Inject constructor(
    private val transferRepository: TransferRepository
) {
    /**
     * Aktualisiert den Fortschritt eines Transfers
     *
     * @param paketId Die ID des Pakets
     * @param peerId Die ID des Peers
     * @param progressPct Der Fortschritt in Prozent (0-100)
     */
    suspend operator fun invoke(paketId: PaketId, peerId: PeerId, progressPct: Int) {
        transferRepository.updateProgress(paketId, peerId, progressPct)
    }
}