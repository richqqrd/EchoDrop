package com.example.echodrop.model.dataLayer.datasource.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Represents a chunk of a file stored in the database.
 *
 * @property chunkId The unique identifier of the chunk.
 * @property fileId The unique identifier of the parent file.
 * @property offset The offset of the chunk within the file.
 * @property size The size of the chunk in bytes.
 * @property completed Indicates whether the chunk has been fully processed.
 */
@Entity(
    tableName = "chunk",
    foreignKeys = [
        ForeignKey(
            entity = FileEntryEntity::class,
            parentColumns = ["fileId"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["fileId"])
    ]
)
data class ChunkEntity(
    @PrimaryKey val chunkId: String,
    val fileId: String,
    val offset: Long,
    val size: Int,
    val completed: Boolean
)