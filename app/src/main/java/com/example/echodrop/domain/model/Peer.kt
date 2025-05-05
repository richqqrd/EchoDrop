package com.example.echodrop.domain.model

/**
 * Represents a peer in the network.
 *
 * @property id The unique identifier of the peer.
 * @property alias An optional alias or nickname for the peer.
 * @property lastSeenUtc The timestamp (in UTC) when the peer was last seen.
 */
data class Peer(
    val id: PeerId,
    val alias: String?,
    val lastSeenUtc: Long
)
