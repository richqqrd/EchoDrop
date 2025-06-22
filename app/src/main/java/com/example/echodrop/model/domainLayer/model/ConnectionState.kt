package com.example.echodrop.model.domainLayer.model

/**
 * Domain model representing the connection state.
 */
data class ConnectionState(
    val isConnected: Boolean = false,
    val connectedDevices: Set<String> = emptySet(),
    val groupOwnerAddress: String? = null,
    val isGroupOwner: Boolean = false
)