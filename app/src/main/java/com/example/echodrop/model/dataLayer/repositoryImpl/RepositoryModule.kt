package com.example.echodrop.model.dataLayer.repositoryImpl


import com.example.echodrop.model.dataLayer.repositoryImpl.FileRepositoryImpl
import com.example.echodrop.model.dataLayer.repositoryImpl.PaketRepositoryImpl
import com.example.echodrop.model.dataLayer.repositoryImpl.TransferRepositoryImpl
import com.example.echodrop.model.domainLayer.repository.FileRepository
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import com.example.echodrop.model.domainLayer.repository.TransferRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPaketRepository(paketRepositoryImpl: PaketRepositoryImpl): PaketRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(fileRepositoryImpl: FileRepositoryImpl): FileRepository

    @Binds
    @Singleton
    abstract fun bindTransferRepository(transferRepositoryImpl: TransferRepositoryImpl): TransferRepository




}