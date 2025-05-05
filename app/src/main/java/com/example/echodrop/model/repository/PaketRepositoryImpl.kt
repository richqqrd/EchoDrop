package com.example.echodrop.model.repository

import com.example.echodrop.model.database.daos.FileEntryDao
import com.example.echodrop.model.database.daos.PaketDao
import com.example.echodrop.domain.model.FileEntry
import com.example.echodrop.domain.model.Paket
import com.example.echodrop.domain.model.PaketId
import com.example.echodrop.domain.model.PaketMeta
import com.example.echodrop.model.database.entities.FileEntryEntity
import com.example.echodrop.model.database.entities.PaketEntity
import com.example.echodrop.model.database.entities.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of the `PaketRepository` interface.
 *
 * @property paketDao The DAO used to access package data in the database.
 * @property fileEntryDao The DAO used to access file entry data in the database.
 */
class PaketRepositoryImpl @Inject constructor(
    private val paketDao: PaketDao,
    private val fileEntryDao: FileEntryDao
) : PaketRepository {
    override fun observeInbox(): Flow<List<Paket>> =
        paketDao.observeAll().map { entityList -> 
            entityList.map { entity -> 
                val files = emptyList<FileEntry>()
                entity.toDomain(files)
            }
        }

    override suspend fun getPaket(id: PaketId): Paket? {
        val entity = paketDao.findById(id.value) ?: return null
        val files = fileEntryDao.findByPaket(id.value).map {it.toDomain() }
        return entity.toDomain(files)
    }

    override suspend fun insert(meta: PaketMeta, files: List<FileEntry>): PaketId {
        val id = PaketId(UUID.randomUUID().toString())
        val totalSize = files.sumOf {it.sizeBytes}

        val paketEntity = PaketEntity(
            paketId = id.value,
            version = 1,
            title = meta.title, 
            description = meta.description, 
            tags = meta.tags,
            sizeBytes = totalSize, 
            sha256 = "",
            fileCount = files.size,
            ttlSeconds = meta.ttlSeconds, 
            priority = meta.priority, 
            hopLimit = null,
            manifestHash = "", 
            createdUtc = System.currentTimeMillis()
        )

    paketDao.upsert(paketEntity)

    val fileEntities = files.mapIndexed {index, file -> 
        FileEntryEntity(
            fileId = UUID.randomUUID().toString(),
            paketOwnerId = id.value, 
            path = file.path, 
            mime = file.mime, 
            sizeBytes = file.sizeBytes,
            orderIdx = file.orderIdx
        )
    }

    fileEntryDao.insertAll(fileEntities)
    return id
    }

    override suspend fun updateMeta(id: PaketId, ttlSeconds: Int, priority: Int) {
        val entity = paketDao.findById(id.value) ?: return
        val updated = entity.copy(
            ttlSeconds = ttlSeconds, 
            priority = priority
        )
        paketDao.upsert(updated)
    }

    override suspend fun delete(id: PaketId) {
        paketDao.deleteById(id.value)
        }

    override suspend fun purgeExpire(nowUtc: Long): Int {
        return paketDao.purgeExpired(nowUtc)
        }


}


