package com.example.echodrop.model.domainLayer.transport


import com.example.echodrop.model.dataLayer.database.daos.TransferDao
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PeerId
import com.example.echodrop.model.domainLayer.transport.TransferProgressCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTransferProgressCallback @Inject constructor(
    private val transferDao: TransferDao
) : TransferProgressCallback {

    override suspend fun updateProgress(paketId: PaketId, peerId: PeerId, progressPct: Int) {
        val currentLog = transferDao.findById(paketId.value, peerId.value) ?: return
        val updatedLog = currentLog.copy(
            progressPct = progressPct,
            lastUpdateUtc = System.currentTimeMillis()
        )
        transferDao.upsert(updatedLog)
    }

}