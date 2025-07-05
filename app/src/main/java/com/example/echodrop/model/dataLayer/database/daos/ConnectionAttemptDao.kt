package com.example.echodrop.model.dataLayer.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.echodrop.model.dataLayer.database.entities.ConnectionAttemptEntity

@Dao
interface ConnectionAttemptDao {
    @Insert
    suspend fun insert(attempt: ConnectionAttemptEntity)

    @Query("""
        SELECT COUNT(*) 
        FROM connection_attempts 
        WHERE deviceAddress = :deviceAddress 
        AND paketId = :paketId 
        AND successful = 0 
        AND timestamp > :minTimestamp
    """)
    suspend fun getFailedAttemptCount(
        deviceAddress: String, 
        paketId: String, 
        minTimestamp: Long
    ): Int

    @Query("DELETE FROM connection_attempts WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
} 