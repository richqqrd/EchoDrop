package com.example.echodrop.model.domainLayer.model

data class ConnectionAttempt(
    val deviceAddress: String,
    val paketId: PaketId,
    val timestamp: Long,
    val successful: Boolean
)