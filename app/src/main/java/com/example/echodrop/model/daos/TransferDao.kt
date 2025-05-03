package com.example.echodrop.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import com.example.echodrop.model.entities.TransferLogEntity

/**
 * Data Access Object for Transfer entities.
 */
@Dao
interface TransferDao {

    /**
     * Observes all transfer logs in the database.
     *
     * @return A `Flow` emitting a list of `TransferLogEntity` objects.
     */
    @Query("SELECT * FROM transfer_log")
    fun observeAll(): Flow<List<TransferLogEntity>>

    /**
     * Inserts or updates a transfer log in the database.
     *
     * If the transfer log already exists, it will be replaced with the new data.
     *
     * @param log The `TransferLogEntity` object to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: TransferLogEntity)

    /**
     * Finds a transfer log by its package ID and peer ID.
     *
     * @param pid The ID of the package associated with the transfer log.
     * @param peer The ID of the peer associated with the transfer log.
     * @return The `TransferLogEntity` object if found, or `null` if not found.
     */
    @Query("SELECT * FROM transfer_log WHERE paketId = :pid AND peerId = :peer")
    suspend fun findById(pid: String, peer: String): TransferLogEntity?

    /**
     * Deletes a transfer log by its package ID and peer ID.
     *
     * @param pid The ID of the package associated with the transfer log.
     * @param peer The ID of the peer associated with the transfer log.
     */
    @Query("DELETE FROM transfer_log WHERE paketId = :pid AND peerId = :peer")
    suspend fun delete(pid: String, peer: String)
}