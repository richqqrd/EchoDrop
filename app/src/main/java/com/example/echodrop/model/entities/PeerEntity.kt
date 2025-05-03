package com.example.echodrop.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peer")
data class PeerEntity(
    @PrimaryKey val peerId: String,
    val alias: String?,
    val lastSeenUtc: Long
)
