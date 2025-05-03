package com.example.echodrop.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.echodrop.model.entities.FileEntryEntity

/**
 * Data Access Object for File entities.
 */
@Dao
interface FileEntryDao {

    /**
     * Retrieves all file entries associated with a specific package, ordered by their index.
     *
     * @param pid The ID of the package for which file entries should be retrieved.
     * @return A list of `FileEntryEntity` objects belonging to the specified package.
     */
    @Query("SELECT * FROM file_entry WHERE paketOwnerId = :pid ORDER BY orderIdx")
    suspend fun findByPaket(pid: String): List<FileEntryEntity>

    /**
     * Inserts or updates a list of file entries in the database.
     *
     * If a file entry already exists, it will be replaced with the new data.
     *
     * @param entries The list of `FileEntryEntity` objects to be inserted or updated.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<FileEntryEntity>)

    /**
     * Deletes all file entries associated with a specific package from the database.
     *
     * @param pid The ID of the package whose file entries should be deleted.
     * @return The number of rows deleted.
     */
    @Query("DELETE FROM file_entry WHERE paketOwnerId = :pid")
    suspend fun deleteByPaket(pid: String): Int
}