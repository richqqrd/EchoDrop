package com.example.echodrop.model.domainLayer.repository

import com.example.echodrop.model.domainLayer.model.PaketId

interface ConnectionAttemptRepository {
    suspend fun trackAttempt(deviceAddress: String, paketId: PaketId, successful: Boolean)
    suspend fun getFailedAttemptCount(deviceAddress: String, paketId: PaketId, minTimestamp: Long): Int
    suspend fun cleanupOldAttempts(cutoffTimestamp: Long)
} 