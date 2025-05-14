package com.example.echodrop.viewmodel

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId

data class TransferLogUi (
    val paketId: PaketId,
    val peerId: PeerId,
    val progressPct: Int,
    val state: String
)