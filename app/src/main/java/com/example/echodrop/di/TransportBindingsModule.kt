package com.example.echodrop.di

import com.example.echodrop.model.dataLayer.impl.transport.ChunkIOImpl
import com.example.echodrop.model.dataLayer.impl.transport.ManifestBuilderImpl
import com.example.echodrop.model.dataLayer.datasource.platform.wifi.WiFiDirectDiscovery
import com.example.echodrop.model.dataLayer.datasource.platform.wifi.WiFiDirectService
import com.example.echodrop.model.dataLayer.impl.transport.TransportManagerImpl
import com.example.echodrop.model.dataLayer.impl.transport.ForwarderImpl
import com.example.echodrop.model.dataLayer.impl.transport.MaintenanceSchedulerImpl
import com.example.echodrop.model.dataLayer.impl.transport.ManifestParserImpl
import com.example.echodrop.model.domainLayer.transport.ChunkIO
import com.example.echodrop.model.domainLayer.transport.DeviceDiscovery
import com.example.echodrop.model.domainLayer.transport.DirectSocketService
import com.example.echodrop.model.domainLayer.transport.ManifestBuilder
import com.example.echodrop.model.domainLayer.transport.TransportManager
import com.example.echodrop.model.domainLayer.transport.DefaultTransferProgressCallback
import com.example.echodrop.model.domainLayer.transport.TransferProgressCallback
import com.example.echodrop.model.domainLayer.transport.Forwarder
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

    @Binds
    @Singleton
    abstract fun bindForwarder(impl: ForwarderImpl): Forwarder
   
    @Binds @Singleton
    abstract fun bindChunkIO(impl: ChunkIOImpl): ChunkIO
    
    @Binds
    @Singleton
    abstract fun bindMaintenanceScheduler(impl: MaintenanceSchedulerImpl): com.example.echodrop.model.domainLayer.transport.MaintenanceScheduler
    
    @Binds
    @Singleton
    abstract fun bindManifestParser(impl: ManifestParserImpl): com.example.echodrop.model.domainLayer.transport.ManifestParser
    
    companion object {
        @Provides
        @Singleton
        fun provideManifestFlow(): MutableSharedFlow<Pair<com.example.echodrop.model.domainLayer.model.PaketId, com.example.echodrop.model.domainLayer.model.PeerId>> = MutableSharedFlow()

        @Provides
        @Singleton
        fun provideForwardEventFlow(): MutableSharedFlow<com.example.echodrop.model.domainLayer.transport.ForwardEvent> = MutableSharedFlow(replay = 0, extraBufferCapacity = 20)
    }
} 