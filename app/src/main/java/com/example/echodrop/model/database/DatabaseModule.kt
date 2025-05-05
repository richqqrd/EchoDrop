package com.example.echodrop.model.database

import android.content.Context
import androidx.room.Room
import com.example.echodrop.model.database.daos.ChunkDao
import com.example.echodrop.model.database.daos.FileEntryDao
import com.example.echodrop.model.database.daos.PaketDao
import com.example.echodrop.model.database.daos.PeerDao
import com.example.echodrop.model.database.daos.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This module supplies the `EchoDatabase` instance and the associated
 * Data Access Objects (DAOs) used to interact with the database.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides a singleton instance of the `EchoDatabase`.
     *
     * @param ctx The application context required to create the database.
     * @return An instance of `EchoDatabase`.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): EchoDatabase =
    Room.databaseBuilder(ctx, EchoDatabase::class.java, "echo.db")
    .fallbackToDestructiveMigration() //nur für Entwicklung
    .build()

    /**
     * Provides the `PaketDao` instance.
     *
     * @param db The `EchoDatabase` instance.
     * @return An instance of `PaketDao`.
     */
    @Provides fun providePaketDao(db: EchoDatabase): PaketDao = db.paketDao()

    /**
     * Provides the `FileEntryDao` instance.
     *
     * @param db The `EchoDatabase` instance.
     * @return An instance of `FileEntryDao`.
     */
    @Provides fun provideFileEntryDao(db: EchoDatabase): FileEntryDao = db.fileEntryDao()

    /**
     * Provides the `ChunkDao` instance.
     *
     * @param db The `EchoDatabase` instance.
     * @return An instance of `ChunkDao`.
     */
    @Provides fun provideChunkDao(db: EchoDatabase): ChunkDao = db.chunkDao()

    /**
     * Provides the `PeerDao` instance.
     *
     * @param db The `EchoDatabase` instance.
     * @return An instance of `PeerDao`.
     */
    @Provides fun providePeerDao(db: EchoDatabase): PeerDao = db.peerDao()

    /**
     * Provides the `TransferDao` instance.
     *
     * @param db The `EchoDatabase` instance.
     * @return An instance of `TransferDao`.
     */
    @Provides fun provideTransferDao(db: EchoDatabase): TransferDao = db.transferDao()


}