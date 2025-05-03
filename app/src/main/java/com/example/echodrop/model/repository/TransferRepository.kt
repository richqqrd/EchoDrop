package com.example.echodrop.model.repository

import com.example.echodrop.model.domain.PaketId
import com.example.echodrop.model.domain.PeerId
import com.example.echodrop.model.domain.TransferLog
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

}