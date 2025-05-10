package com.example.echodrop.model.dataLayer.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a peer in the system.
 *
 * @property peerId The unique identifier of the peer.
 * @property alias An optional alias or nickname for the peer.
 * @property lastSeenUtc The timestamp (in UTC) when the peer was last seen.
 */
@Entity(tableName = "peer")
data class PeerEntity(
    @PrimaryKey val peerId: String,
    val alias: String?,
    val lastSeenUtc: Long
)
