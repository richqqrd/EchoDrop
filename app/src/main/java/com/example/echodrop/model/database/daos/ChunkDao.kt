package com.example.echodrop.model.database.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.echodrop.model.database.entities.ChunkEntity


/**
 * Data Access Object for chunk entities.
 */
@Dao
interface ChunkDao {

    /**
     * Retrieves all chunks for a given file, ordered by offset.
     *
     * @param fid The file ID.
     * @return A list of chunk entities.
     */
    @Query("SELECT * FROM chunk WHERE fileId = :fid ORDER BY \"offset\"")
    suspend fun findByFile(fid: String): List<ChunkEntity>

    /**
     * Inserts or updates a list of chunks in the database.
     *
     * If a chunk already exists, it will be replaced with the new data.
     *
     * @param chunks The list of `ChunkEntity` objects to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChunks(chunks: List<ChunkEntity>)

    /**
     * Deletes all chunks associated with a specific file from the database.
     *
     * @param fid The ID of the file whose chunks should be deleted.
     */
    @Query("DELETE FROM chunk WHERE fileId = :fid")
    suspend fun deleteByFile(fid: String): Int
}