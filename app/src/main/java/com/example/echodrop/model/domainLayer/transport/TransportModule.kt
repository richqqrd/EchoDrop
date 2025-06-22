package com.example.echodrop.di

import com.example.echodrop.model.domainLayer.transport.DefaultTransferProgressCallback  // Korrekter Import
import com.example.echodrop.model.domainLayer.transport.TransferProgressCallback
import com.example.echodrop.model.domainLayer.transport.TransportManager
import com.example.echodrop.model.domainLayer.transport.TransportManagerImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransportModule {
    @Binds
    @Singleton
    abstract fun bindTransportManager(
        impl: TransportManagerImpl
    ): TransportManager

    @Binds
    @Singleton
    abstract fun bindTransferProgressCallback(
        impl: DefaultTransferProgressCallback
    ): TransferProgressCallback

    companion object {
        @Provides
        @Singleton
        fun provideMutableSharedFlow(): MutableSharedFlow<Pair<com.example.echodrop.model.domainLayer.model.PaketId, com.example.echodrop.model.domainLayer.model.PeerId>> {
            return MutableSharedFlow()
        }

    }


}