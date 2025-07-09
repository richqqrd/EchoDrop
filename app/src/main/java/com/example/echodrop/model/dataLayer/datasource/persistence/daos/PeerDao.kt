package com.example.echodrop.model.dataLayer.datasource.persistence.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.PeerEntity

/**
 * Data Access Object for Peer entities.
 */
@Dao
interface PeerDao {

    /**
     * Observes all peers in the database, ordered by their last seen time in descending order.
     *
     * @return A `Flow` emitting a list of `PeerEntity` objects.
     */
    @Query("SELECT * FROM peer ORDER BY lastSeenUtc DESC")
    fun observeAll(): Flow<List<PeerEntity>>

    /**
     * Inserts or updates a peer in the database.
     *
     * If the peer already exists, it will be replaced with the new data.
     *
     * @param peer The `PeerEntity` object to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(peer: PeerEntity)

    /**
     * Deletes all peers that have not been seen since a specified cutoff time.
     *
     * @param cutoffUtc The cutoff UTC time in milliseconds. Peers last seen before this time will be deleted.
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM peer WHERE lastSeenUtc < :cutoffUtc")
    suspend fun purgeStale(cutoffUtc: Long): Int
}