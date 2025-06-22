package com.example.echodrop.model.dataLayer.repositoryImpl

import com.example.echodrop.model.dataLayer.database.daos.TransferDao
import com.example.echodrop.model.domainLayer.model.TransferState
import com.example.echodrop.model.dataLayer.database.entities.TransferLogEntity
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.model.TransferDirection
import com.example.echodrop.model.domainLayer.model.TransferLog
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
    private val transferDao: TransferDao,
    
) : TransferRepository {
    override fun observeTransfers(): Flow<List<TransferLog>> {
        return transferDao.observeAll().map{entityList ->
            entityList.map {entity ->
                TransferLog(
                    paketId = PaketId(entity.paketId),
                    peerId = PeerId(entity.peerId),
                    state = entity.state,
                    progressPct = entity.progressPct,
                    lastUpdateUtc = entity.lastUpdateUtc,
                    direction = determineDirection(entity.peerId)

                )
            }}
    }

    override suspend fun startTransfer(paketId: PaketId, peerId: PeerId) {
        val now = System.currentTimeMillis()
        val existing = transferDao.findById(paketId.value, peerId.value)
        val toSave = if (existing != null) {
            existing.copy(state = TransferState.ACTIVE, lastUpdateUtc = now)
        } else {
            TransferLogEntity(
                paketId       = paketId.value,
                peerId        = peerId.value,
                state         = TransferState.ACTIVE,
                progressPct   = 0,
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
        transferDao.delete(paketId.value, peerId.value)
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
        state: com.example.echodrop.model.domainLayer.model.TransferState
    ) {
        val currentLog = transferDao.findById(paketId.value, peerId.value) ?: return
        val updatedLog = currentLog.copy(
            state = state,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(updatedLog)
    }


    private fun determineDirection(peerId: String): TransferDirection {
        // Hier deine Logik zur Bestimmung der Richtung
        // Beispiel: Alle PeerIDs, die mit "out-" beginnen, sind ausgehend
        return if (peerId.startsWith("out-")) {
            TransferDirection.OUTGOING
        } else {
            TransferDirection.INCOMING
        }
    }


}