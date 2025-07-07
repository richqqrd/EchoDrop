package com.example.echodrop.model.dataLayer.repositoryImpl

import com.example.echodrop.model.dataLayer.database.daos.FileEntryDao
import com.example.echodrop.model.dataLayer.database.daos.PaketDao
import com.example.echodrop.model.dataLayer.database.entities.FileEntryEntity
import com.example.echodrop.model.dataLayer.database.entities.PaketEntity
import com.example.echodrop.model.dataLayer.database.entities.toDomain
import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.model.PaketMeta
import com.example.echodrop.model.domainLayer.repository.PaketRepository
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
            hopLimit = meta.maxHops,
            currentHopCount = 0,
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
        val deleted = paketDao.purgeExpired(nowUtc)
        android.util.Log.d("PaketRepositoryImpl", "purgeExpire deleted $deleted paket(s) older than $nowUtc")
        return deleted
    }

        override suspend fun upsert(paket: Paket) {
    // Speichere das Paket-Entity
    val paketEntity = PaketEntity(
        paketId = paket.id.value,
        version = 1,
        title = paket.meta.title, 
        description = paket.meta.description, 
        tags = paket.meta.tags,
        sizeBytes = paket.files.sumOf { it.sizeBytes }, 
        sha256 = paket.sha256 ?: "",
        fileCount = paket.files.size,
        ttlSeconds = paket.meta.ttlSeconds, 
        priority = paket.meta.priority, 
        hopLimit = paket.meta.maxHops,
        currentHopCount = paket.currentHopCount,
        manifestHash = "", 
        createdUtc = paket.createdUtc
    )
    paketDao.upsert(paketEntity)
    
    // Lösche vorhandene Dateien für dieses Paket
    fileEntryDao.deleteByPaket(paket.id.value)
    
    // Speichere die Dateien
    val fileEntities = paket.files.map { file -> 
        FileEntryEntity(
            fileId = UUID.randomUUID().toString(),
            paketOwnerId = paket.id.value,
            path = file.path, 
            mime = file.mime, 
            sizeBytes = file.sizeBytes,
            orderIdx = file.orderIdx
        )
    }
    fileEntryDao.insertAll(fileEntities)
}

}


