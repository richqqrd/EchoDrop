package com.example.echodrop.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.echodrop.model.domain.FileEntry

/**
 * Represents a file entry stored in the database.
 *
 * @property fileId The unique identifier of the file.
 * @property paketOwnerId The unique identifier of the package owner associated with the file.
 * @property path The file path.
 * @property mime The MIME type of the file.
 * @property sizeBytes The size of the file in bytes.
 * @property orderIdx The order index of the file within its package.
 */
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

/**
 * Converts a `FileEntryEntity` to its domain representation.
 *
 * @return The domain representation of the file entry.
 */
fun FileEntryEntity.toDomain(): FileEntry = FileEntry(
    path = path,
    mime = mime,
    sizeBytes = sizeBytes,
    orderIdx = orderIdx
)