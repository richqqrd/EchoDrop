package com.example.echodrop.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "chunk",
    foreignKeys = [
        ForeignKey(
            entity = FileEntryEntity::class,
            parentColumns = ["fileId"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChunkEntity(
    @PrimaryKey val chunkId: String,
    val fileId: String,
    val offset: Long,
    val size: Int,
    val sha256: String,
    val completed: Boolean
)