package com.example.echodrop.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.echodrop.model.entities.FileEntryEntity

@Dao
interface FileEntryDao {
    @Query("SELECT * FROM file_entry WHERE paketOwnerId = :pid ORDER BY orderIdx")
    suspend fun findByPaket(pid: String): List<FileEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FileEntryEntity>)

    @Query("DELETE FROM file_entry WHERE paketOwnerId = :pid")
    suspend fun deleteByPaket(pid: String): Int
}