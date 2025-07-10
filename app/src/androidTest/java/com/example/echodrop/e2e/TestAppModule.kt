package com.example.echodrop.e2e

import android.content.Context
import androidx.room.Room
import com.example.echodrop.model.dataLayer.datasource.persistence.EchoDatabase
import com.example.echodrop.model.dataLayer.datasource.persistence.daos.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.hilt.testing.TestInstallIn
import com.example.echodrop.model.dataLayer.datasource.persistence.DatabaseModule

@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
@Module
object TestAppModule {

    @Provides
    @Singleton
    fun provideInMemoryDb(@ApplicationContext ctx: Context): EchoDatabase =
        Room.inMemoryDatabaseBuilder(ctx, EchoDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    /* ---------- DAOs für RepositoryModule ---------- */
    @Provides fun providePaketDao(db: EchoDatabase): PaketDao = db.paketDao()
    @Provides fun provideFileEntryDao(db: EchoDatabase): FileEntryDao = db.fileEntryDao()
    @Provides fun provideTransferDao(db: EchoDatabase): TransferDao = db.transferDao()
    @Provides fun provideConnectionAttemptDao(db: EchoDatabase): ConnectionAttemptDao = db.connectionAttemptDao()
    @Provides fun providePeerDao(db: EchoDatabase): PeerDao = db.peerDao()

    // Keine WiFi-Fakes nötig – wir verwenden die Original-Bindings
} 