package com.example.echodrop.model.domain

data class TransferLog(
    val paketId: PaketId,
    val peerId: PeerId,
    val state: TransferState,
    val progressPct: Int,
    val lastUpdateUtc: Long
)
