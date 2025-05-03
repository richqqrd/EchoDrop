package com.example.echodrop.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import com.example.echodrop.model.entities.PaketEntity

@Dao
interface PaketDao {
    @Query("SELECT * FROM paket ORDER BY createdUtc DESC")
    fun observeAll(): Flow<List<PaketEntity>>

    @Query("SELECT * FROM paket WHERE paketId = :id")
    suspend fun findById(id: String): PaketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(paket: PaketEntity)

    @Query("DELETE FROM paket WHERE paketId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM paket WHERE createdUtc + ttlSeconds*1000 < :nowUtc")
    suspend fun purgeExpired(nowUtc: Long): Int
}