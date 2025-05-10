package com.example.echodrop.model.dataLayer.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.echodrop.model.domainLayer.model.TransferState

/**
 * Represents a peer in the system.
 *
 * @property peerId The unique identifier of the peer.
 * @property alias An optional alias or nickname for the peer.
 * @property lastSeenUtc The timestamp (in UTC) when the peer was last seen.
 */
@Entity(
    tableName = "transfer_log",
    primaryKeys = ["paketId", "peerId"],
    foreignKeys = [
        ForeignKey(
            entity = PaketEntity::class,
            parentColumns = ["paketId"],
            childColumns = ["paketId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PeerEntity::class,
            parentColumns = ["peerId"],
            childColumns = ["peerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["paketId"]),
        Index(value = ["peerId"])
    ]
)
data class TransferLogEntity(
    val paketId: String,
    val peerId: String,
    val state: TransferState,
    val progressPct: Int,
    val lastUpdateUtc: Long
)