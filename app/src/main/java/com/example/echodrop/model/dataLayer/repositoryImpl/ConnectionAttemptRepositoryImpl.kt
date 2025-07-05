package com.example.echodrop.model.dataLayer.repositoryImpl

import com.example.echodrop.model.dataLayer.database.daos.ConnectionAttemptDao
import com.example.echodrop.model.dataLayer.database.entities.ConnectionAttemptEntity
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.repository.ConnectionAttemptRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionAttemptRepositoryImpl @Inject constructor(
    private val connectionAttemptDao: ConnectionAttemptDao
) : ConnectionAttemptRepository {

    override suspend fun trackAttempt(deviceAddress: String, paketId: PaketId, successful: Boolean) {
        val entity = ConnectionAttemptEntity(
            deviceAddress = deviceAddress,
            paketId = paketId.value,
            timestamp = System.currentTimeMillis(),
            successful = successful
        )
        connectionAttemptDao.insert(entity)
    }

    override suspend fun getFailedAttemptCount(
        deviceAddress: String,
        paketId: PaketId,
        minTimestamp: Long
    ): Int {
        return connectionAttemptDao.getFailedAttemptCount(
            deviceAddress = deviceAddress,
            paketId = paketId.value,
            minTimestamp = minTimestamp
        )
    }

    override suspend fun cleanupOldAttempts(cutoffTimestamp: Long) {
        connectionAttemptDao.deleteOlderThan(cutoffTimestamp)
    }
} 