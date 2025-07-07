package com.example.echodrop.di

import com.example.echodrop.model.dataLayer.transport.ChunkIOImpl
import com.example.echodrop.model.dataLayer.transport.ManifestBuilderImpl
import com.example.echodrop.model.dataLayer.transport.WiFiDirectDiscovery
import com.example.echodrop.model.dataLayer.transport.WiFiDirectService
import com.example.echodrop.model.dataLayer.transport.TransportManagerImpl
import com.example.echodrop.model.domainLayer.transport.ChunkIO
import com.example.echodrop.model.domainLayer.transport.DeviceDiscovery
import com.example.echodrop.model.domainLayer.transport.DirectSocketService
import com.example.echodrop.model.domainLayer.transport.ManifestBuilder
import com.example.echodrop.model.domainLayer.transport.TransportManager
import com.example.echodrop.model.domainLayer.transport.DefaultTransferProgressCallback
import com.example.echodrop.model.domainLayer.transport.TransferProgressCallback
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransportBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTransportManager(impl: TransportManagerImpl): TransportManager

    @Binds
    @Singleton
    abstract fun bindProgressCallback(impl: DefaultTransferProgressCallback): TransferProgressCallback

    @Binds
    @Singleton
    abstract fun bindDeviceDiscovery(impl: WiFiDirectDiscovery): DeviceDiscovery

    @Binds
    @Singleton
    abstract fun bindSocketService(impl: WiFiDirectService): DirectSocketService

    @Binds
    @Singleton
    abstract fun bindManifestBuilder(impl: ManifestBuilderImpl): ManifestBuilder
   
    @Binds @Singleton
    abstract fun bindChunkIO(impl: ChunkIOImpl): ChunkIO
    
    companion object {
        @Provides
        @Singleton
        fun provideManifestFlow(): MutableSharedFlow<Pair<com.example.echodrop.model.domainLayer.model.PaketId, com.example.echodrop.model.domainLayer.model.PeerId>> = MutableSharedFlow()

        @Provides
        @Singleton
        fun provideForwardEventFlow(): MutableSharedFlow<com.example.echodrop.model.domainLayer.transport.ForwardEvent> = MutableSharedFlow(replay = 0, extraBufferCapacity = 20)
    }
} 