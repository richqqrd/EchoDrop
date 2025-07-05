package com.example.echodrop.model.dataLayer.database.entities

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "connection_attempts",
    primaryKeys = ["deviceAddress", "paketId", "timestamp"],
    indices = [
        Index(value = ["paketId"]),
        Index(value = ["deviceAddress"])
    ]
)
data class ConnectionAttemptEntity(
    val deviceAddress: String,
    val paketId: String,
    val timestamp: Long,
    val successful: Boolean
) 