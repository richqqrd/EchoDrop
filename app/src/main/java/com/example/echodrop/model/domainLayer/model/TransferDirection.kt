package com.example.echodrop.model.domainLayer.model

/**
 * Represents the direction of a transfer operation.
 */
enum class TransferDirection {
    /**
     * Transfer is outgoing (sending from this device to a peer)
     */
    OUTGOING,

    /**
     * Transfer is incoming (receiving from a peer to this device)
     */
    INCOMING
}