package com.example.echodrop.model.dataLayer.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.echodrop.model.dataLayer.database.daos.ChunkDao
import com.example.echodrop.model.dataLayer.database.daos.ConnectionAttemptDao
import com.example.echodrop.model.dataLayer.database.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.database.daos.PaketDao
import com.example.echodrop.model.dataLayer.database.daos.PeerDao
import com.example.echodrop.model.dataLayer.database.daos.TransferDao
import com.example.echodrop.model.dataLayer.database.entities.ChunkEntity
import com.example.echodrop.model.dataLayer.database.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.database.entities.PaketEntity
import com.example.echodrop.model.dataLayer.database.entities.PeerEntity
import com.example.echodrop.model.dataLayer.database.entities.TransferLogEntity
import com.example.echodrop.model.dataLayer.database.entities.ConnectionAttemptEntity
import com.example.echodrop.model.domainLayer.model.Converters

/**
 * Main database class for the Echo application.
 *
 * This class defines the Room database configuration and serves as the main
 * access point for the underlying SQLite database. It includes all the entities
 * and DAOs required for database operations.
 */
@Database(
    version = 3,
    exportSchema = false,
    entities = [
        PaketEntity::class,
        FileEntryEntity::class,
        ChunkEntity::class,
        PeerEntity::class,
        TransferLogEntity::class,
        ConnectionAttemptEntity::class
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

    abstract fun connectionAttemptDao(): ConnectionAttemptDao
}