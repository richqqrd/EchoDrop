package com.example.echodrop.domain.model

/**
 * Represents a log entry for a transfer operation.
 *
 * @property paketId The unique identifier of the package being transferred.
 * @property peerId The unique identifier of the peer involved in the transfer.
 * @property state The current state of the transfer.
 * @property progressPct The progress of the transfer as a percentage.
 * @property lastUpdateUtc The timestamp (in UTC) of the last update to the transfer.
 */
data class TransferLog(
    val paketId: PaketId,
    val peerId: PeerId,
    val state: TransferState,
    val progressPct: Int,
    val lastUpdateUtc: Long
)
