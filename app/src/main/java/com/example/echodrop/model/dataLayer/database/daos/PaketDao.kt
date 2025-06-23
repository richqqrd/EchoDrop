package com.example.echodrop.model.dataLayer.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import com.example.echodrop.model.dataLayer.database.entities.PaketEntity

/**
 * Data Access Object for Packet entities.
 */
@Dao
interface PaketDao {

    /**
     * Observes all pakets in the database, ordered by their creation time in descending order.
     *
     * @return A `Flow` emitting a list of `PaketEntity` objects.
     */
@Query("SELECT * FROM paket ORDER BY priority DESC, createdUtc DESC")
    fun observeAll(): Flow<List<PaketEntity>>

    /**
     * Finds a paket by its ID.
     *
     * @param id The ID of the paket to retrieve.
     * @return The `PaketEntity` object if found, or `null` if not found.
     */
    @Query("SELECT * FROM paket WHERE paketId = :id")
    suspend fun findById(id: String): PaketEntity?

    /**
     * Inserts or updates a paket in the database.
     *
     * If the paket already exists, it will be replaced with the new data.
     *
     * @param paket The `PaketEntity` object to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(paket: PaketEntity)

    /**
     * Deletes a paket by its ID.
     *
     * @param id The ID of the paket to delete.
     */
    @Query("DELETE FROM paket WHERE paketId = :id")
    suspend fun deleteById(id: String)

    /**
     * Deletes all expired pakets from the database.
     *
     * A paket is considered expired if its creation time plus its TTL (time-to-live) has passed.
     *
     * @param nowUtc The current UTC time in milliseconds.
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM paket WHERE createdUtc + ttlSeconds*1000 < :nowUtc")
    suspend fun purgeExpired(nowUtc: Long): Int
}