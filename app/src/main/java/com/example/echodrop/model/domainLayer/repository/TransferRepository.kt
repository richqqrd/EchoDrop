package com.example.echodrop.model.domainLayer.repository

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferLog
import com.example.echodrop.model.domainLayer.model.TransferState
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing transfer-related operations
 */
interface TransferRepository {

    /**
     * Observes the list of transfer logs.
     *
     * @return A flow emitting a list of transfer logs.
     */
    fun observeTransfers(): Flow<List<TransferLog>>

    /**
     * Starts a transfer for the specified package.
     *
     * @param paketId The unique identifier of the package being transferred.
     * @param peerId The unique identifier of the peer involved in the transfer.
     * @param direction The direction of the transfer.
     */
    suspend fun startTransfer(
        paketId: PaketId,
        peerId: PeerId,
        direction: com.example.echodrop.model.domainLayer.model.TransferDirection = com.example.echodrop.model.domainLayer.model.TransferDirection.INCOMING
    )

    /**
     * Pauses a transfer for the specified package and peer.
     *
     * @param paketId The unique identifier of the package being transferred.
     * @param peerId The unique identifier of the peer involved in the transfer.
     */
    suspend fun pause(paketId: PaketId, peerId: PeerId)

    /**
     * Resumes a transfer for the specified package and peer.
     *
     * @param paketId The unique identifier of the package being transferred.
     * @param peerId The unique identifier of the peer involved in the transfer.
     */
    suspend fun resume(paketId: PaketId, peerId: PeerId)

    /**
     * Cancels a transfer for the specified package and peer.
     *
     * @param paketId The unique identifier of the package being transferred.
     * @param peerId The unique identifier of the peer involved in the transfer.
     */
    suspend fun cancel(paketId: PaketId, peerId: PeerId)

    /**
     * Updates the progress percentage of a transfer.
     *
     * @param paketId The unique identifier of the package being transferred.
     * @param peerId The unique identifier of the peer involved in the transfer.
     * @param progressPct The progress percentage (0-100).
     */
    suspend fun updateProgress(paketId: PaketId, peerId: PeerId, progressPct: Int)

/**
 * Aktualisiert den Status eines Transfers
 *
 * @param paketId Die ID des Pakets
 * @param peerId Die ID des Peers
 * @param state Der neue Status
 */
suspend fun updateState(paketId: PaketId, peerId: PeerId, state: TransferState)
}