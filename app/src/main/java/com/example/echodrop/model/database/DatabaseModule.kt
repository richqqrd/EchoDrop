package com.example.echodrop.model.database

import android.content.Context
import androidx.room.Room
import com.example.echodrop.model.daos.ChunkDao
import com.example.echodrop.model.daos.FileEntryDao
import com.example.echodrop.model.daos.PaketDao
import com.example.echodrop.model.daos.PeerDao
import com.example.echodrop.model.daos.TransferDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): EchoDatabase =
    Room.databaseBuilder(ctx, EchoDatabase::class.java, "echo.db")
    .fallbackToDestructiveMigration() //nur für Entwicklung
    .build()

    @Provides fun providePaketDao(db: EchoDatabase): PaketDao = db.paketDao()
   @Provides fun provideFileEntryDao(db: EchoDatabase): FileEntryDao = db.fileEntryDao()
   @Provides fun provideChunkDao(db: EchoDatabase): ChunkDao = db.chunkDao()
    @Provides fun providePeerDao(db: EchoDatabase): PeerDao = db.peerDao()
       @Provides fun provideTransferLogDao(db: EchoDatabase): TransferDao = db.transferDao()


}