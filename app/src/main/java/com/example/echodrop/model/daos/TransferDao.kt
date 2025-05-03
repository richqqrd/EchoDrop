package com.example.echodrop.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import com.example.echodrop.model.entities.TransferLogEntity

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer_log")
    fun observeAll(): Flow<List<TransferLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: TransferLogEntity)

    @Query("SELECT * FROM transfer_log WHERE paketId = :pid AND peerId = :peer")
    suspend fun findById(pid: String, peer: String): TransferLogEntity?

    @Query("DELETE FROM transfer_log WHERE paketId = :pid AND peerId = :peer")
    suspend fun delete(pid: String, peer: String)
}