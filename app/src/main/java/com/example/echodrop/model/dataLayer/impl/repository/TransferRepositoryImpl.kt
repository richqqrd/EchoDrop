package com.example.echodrop.model.dataLayer.impl.repository

import android.util.Log
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.TransferDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.TransferLogEntity
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferDirection
import com.example.echodrop.model.domainLayer.model.TransferLog
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.domainLayer.repository.TransferRepository
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
) : TransferRepository {
    override fun observeTransfers(): Flow<List<TransferLog>> {
        return transferDao.observeAll().map { list ->
            list.map { entity ->
                TransferLog(
                    paketId = PaketId(entity.paketId),
                    peerId = PeerId(entity.peerId),
                    state = entity.state,
                    direction = entity.direction,
                    progressPct = entity.progressPct,
                    lastUpdateUtc = entity.lastUpdateUtc
                )
            }
        }
    }

    override suspend fun startTransfer(
        paketId: PaketId,
        peerId: PeerId,
        direction: TransferDirection
    ) {
        val now = System.currentTimeMillis()
        val existing = transferDao.findById(paketId.value, peerId.value)
        val toSave = if (existing != null) {
            existing.copy(state = TransferState.ACTIVE, lastUpdateUtc = now, direction = direction)
        } else {
            TransferLogEntity(
                paketId = paketId.value,
                peerId = peerId.value,
                state = TransferState.ACTIVE,
                direction = direction,
                progressPct = 0,
                lastUpdateUtc = now
            )
        }
        transferDao.upsert(toSave)
    }

    override suspend fun pause(paketId: PaketId, peerId: PeerId) {
        val currentLog = transferDao.findById(paketId.value, peerId.value) ?: return
        val updatedLog = currentLog.copy(
            state = TransferState.PAUSED,
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
        val deleted = transferDao.delete(paketId.value, peerId.value)
        Log.d("TransferRepositoryImpl", "cancel removed $deleted transfer rows for paket=${paketId.value} peer=${peerId.value}")
    }

    override suspend fun updateProgress(paketId: PaketId, peerId: PeerId, progressPct: Int) {
        val currentLog = transferDao.findById(paketId.value, peerId.value) ?: return
        val updatedLog = currentLog.copy(
            progressPct = progressPct,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(updatedLog)
    }

    override suspend fun updateState(
        paketId: PaketId,
        peerId: PeerId,
        state: TransferState
    ) {
        val currentLog = transferDao.findById(paketId.value, peerId.value) ?: return
        val updatedLog = currentLog.copy(
            state = state,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(updatedLog)
    }
}