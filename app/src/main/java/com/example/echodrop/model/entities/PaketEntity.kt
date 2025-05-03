package com.example.echodrop.model.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.echodrop.model.domain.FileEntry
import com.example.echodrop.model.domain.Paket
import com.example.echodrop.model.domain.PaketId
import com.example.echodrop.model.domain.PaketMeta

@Entity(tableName = "paket")
data class PaketEntity(
    @PrimaryKey val paketId: String,
    val version: Int,
    val title: String,
    val description: String?,
    val tags: List<String>,
    val sizeBytes: Long,
    val sha256: String,
    val fileCount: Int,
    val ttlSeconds: Int,
    val priority: Int,
    val hopLimit: Int?,
    val manifestHash: String,
    val createdUtc: Long
    )


fun PaketEntity.toDomain(files: List<FileEntry>): Paket = Paket(
    id              = PaketId(paketId),
    meta            = PaketMeta(title, description, tags, ttlSeconds, priority),
    sizeBytes       = sizeBytes,
    sha256          = sha256,
    fileCount       = fileCount,
    createdUtc      = createdUtc,
    files           = files
)