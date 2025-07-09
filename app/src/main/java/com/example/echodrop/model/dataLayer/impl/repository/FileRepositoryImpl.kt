package com.example.echodrop.model.dataLayer.impl.repository

import com.example.echodrop.model.dataLayer.datasource.persistence.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.datasource.persistence.entities.toDomain
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.repository.FileRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of the `FileRepository` interface.
 *
 * @property fileEntryDao The DAO used to access file entry data in the database.
 */
class FileRepositoryImpl @Inject constructor(
    private val fileEntryDao: FileEntryDao
) : FileRepository {
    override suspend fun getFilesFor(paketId: PaketId): List<FileEntry> {
        return fileEntryDao.findByPaket(paketId.value).map{it.toDomain()}
    }

    override suspend fun insertAll(paketId: PaketId, files: List<FileEntry>) {
        val fileEntities = files.map{file ->
            FileEntryEntity(
                fileId = UUID.randomUUID().toString(),
                paketOwnerId = paketId.value,
                path = file.path,
                mime = file.mime,
                sizeBytes = file.sizeBytes,
                orderIdx = file.orderIdx
            )
        }
        fileEntryDao.insertAll(fileEntities)

        }

    override suspend fun deleteFor(paketId: PaketId) {
        fileEntryDao.deleteByPaket(paketId.value)
        }

}