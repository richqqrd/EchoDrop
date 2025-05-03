package com.example.echodrop.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.echodrop.model.entities.ChunkEntity

@Dao
interface ChunkDao {
    @Query("SELECT * FROM chunk WHERE fileId = :fid ORDER BY \"offset\"")
    suspend fun findByFile(fid: String): List<ChunkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChunks(chunks: List<ChunkEntity>)

    @Query("DELETE FROM chunk WHERE fileId = :fid")
    suspend fun deleteByFile(fid: String)
}