package com.example.echodrop.model.domain

data class Peer(
    val id: PeerId,
    val alias: String?,
    val lastSeenUtc: Long
)
