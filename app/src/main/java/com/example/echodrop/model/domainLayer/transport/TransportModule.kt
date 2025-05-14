package com.example.echodrop.di

import com.example.echodrop.model.domainLayer.transport.TransportManager
import com.example.echodrop.model.domainLayer.transport.TransportManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransportModule {

    @Binds
    @Singleton
    abstract fun bindTransportManager(
        impl: TransportManagerImpl
    ): TransportManager
}