package com.example.echodrop.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.echodrop.model.domain.FileEntry

@Entity(
    tableName = "file_entry",
    foreignKeys = [
        ForeignKey(
            entity = PaketEntity::class,
            parentColumns = ["paketId"],
            childColumns = ["paketOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("paketOwnerId")]
)
data class FileEntryEntity(
    @PrimaryKey val fileId: String,
    val paketOwnerId: String,
    val path: String,
    val mime: String,
    val sizeBytes: Long,
    val orderIdx: Int
)

fun FileEntryEntity.toDomain(): FileEntry = FileEntry(
    path = path,
    mime = mime,
    sizeBytes = sizeBytes,
    orderIdx = orderIdx
)