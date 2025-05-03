package com.example.echodrop.model.repository

import com.example.echodrop.model.domain.PaketId
import com.example.echodrop.model.domain.PeerId
import com.example.echodrop.model.domain.TransferLog
import kotlinx.coroutines.flow.Flow

interface TransferRepository {

    fun observeTransfers(): Flow<List<TransferLog>>

    suspend fun pause(paketId: PaketId, peerId: PeerId)
    suspend fun resume(paketId: PaketId, peerId: PeerId)
    suspend fun cancel(paketId: PaketId, peerId: PeerId)

}