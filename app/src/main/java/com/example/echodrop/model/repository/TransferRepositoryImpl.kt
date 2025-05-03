package com.example.echodrop.model.repository

import com.example.echodrop.model.daos.TransferDao
import com.example.echodrop.model.domain.PaketId
import com.example.echodrop.model.domain.PeerId
import com.example.echodrop.model.domain.TransferLog
import com.example.echodrop.model.domain.TransferState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Implementation of the `TransferRepository` interface.
 *
 * @property transferDao The DAO used to access transfer data in the database.
 */
class TransferRepositoryImpl @Inject constructor(
    private val transferDao: TransferDao
) : TransferRepository{
    override fun observeTransfers(): Flow<List<TransferLog>> {
        return transferDao.observeAll().map{entityList ->
            entityList.map {entity ->
                TransferLog(
                    paketId = PaketId(entity.paketId),
                    peerId = PeerId(entity.peerId),
                    state = entity.state,
                    progressPct = entity.progressPct, 
                    lastUpdateUtc = entity.lastUpdateUtc
                )}}
    }

    override suspend fun pause(paketId: PaketId, peerId: PeerId) {
        val currentLog = transferDao.findById(paketId.value, peerId.value) ?: return
        val updatedLog = currentLog.copy(
            state = TransferState.QUEUED,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(updatedLog)
        }

    override suspend fun resume(paketId: PaketId, peerId: PeerId) {
        val currentLog = transferDao.findById(paketId.value, peerId.value) ?: return
        val updatedLog = currentLog.copy(
            state = TransferState.ACTIVE,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(updatedLog)
    }

    override suspend fun cancel(paketId: PaketId, peerId: PeerId) {
        transferDao.delete(paketId.value, peerId.value)
    }


}