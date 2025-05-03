package com.example.echodrop.model.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import com.example.echodrop.model.entities.PeerEntity

@Dao
interface PeerDao {
    @Query("SELECT * FROM peer ORDER BY lastSeenUtc DESC")
    fun observeAll(): Flow<List<PeerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(peer: PeerEntity)

    @Query("DELETE FROM peer WHERE lastSeenUtc > :cutoffUtc")
    suspend fun purgeStale(cutoffUtc: Long): Int
}