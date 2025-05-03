package com.example.echodrop.model.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.echodrop.model.daos.ChunkDao
import com.example.echodrop.model.daos.FileEntryDao
import com.example.echodrop.model.daos.PaketDao
import com.example.echodrop.model.daos.PeerDao
import com.example.echodrop.model.daos.TransferDao
import com.example.echodrop.model.entities.ChunkEntity
import com.example.echodrop.model.entities.FileEntryEntity
import com.example.echodrop.model.entities.PaketEntity
import com.example.echodrop.model.entities.PeerEntity
import com.example.echodrop.model.entities.TransferLogEntity

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        PaketEntity::class,
        FileEntryEntity::class,
        ChunkEntity::class, 
        PeerEntity::class,
        TransferLogEntity::class
    ]
)

@TypeConverters(Converters::class)
abstract class EchoDatabase : RoomDatabase(){
    abstract fun paketDao(): PaketDao
    abstract fun fileEntryDao(): FileEntryDao
    abstract fun chunkDao(): ChunkDao
    abstract fun peerDao(): PeerDao
    abstract fun transferDao(): TransferDao
}