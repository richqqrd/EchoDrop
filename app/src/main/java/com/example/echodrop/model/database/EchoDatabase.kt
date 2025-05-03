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

/**
 * Main database class for the Echo application.
 *
 * This class defines the Room database configuration and serves as the main
 * access point for the underlying SQLite database. It includes all the entities
 * and DAOs required for database operations.
 */
@Database(
    version = 1,
    exportSchema = false,
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

    /**
     * Provides access to the `PaketDao` for performing operations on the `PaketEntity`.
     *
     * @return An instance of `PaketDao`.
     */
    abstract fun paketDao(): PaketDao

    /**
     * Provides access to the `FileEntryDao` for performing operations on the `FileEntryEntity`.
     *
     * @return An instance of `FileEntryDao`.
     */
    abstract fun fileEntryDao(): FileEntryDao

    /**
     * Provides access to the `ChunkDao` for performing operations on the `ChunkEntity`.
     *
     * @return An instance of `ChunkDao`.
     */
    abstract fun chunkDao(): ChunkDao

    /**
     * Provides access to the `PeerDao` for performing operations on the `PeerEntity`.
     *
     * @return An instance of `PeerDao`.
     */
    abstract fun peerDao(): PeerDao

    /**
     * Provides access to the `TransferDao` for performing operations on the `TransferLogEntity`.
     *
     * @return An instance of `TransferDao`.
     */
    abstract fun transferDao(): TransferDao
}