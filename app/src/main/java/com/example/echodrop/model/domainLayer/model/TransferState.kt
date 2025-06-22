package com.example.echodrop.model.domainLayer.model

/**
 * Represents the possible states of a transfer.
 */
enum class TransferState {
    /**
     * Transfer is currently active and in progress
     */
    ACTIVE,

    /**
     * Transfer is paused by the user
     */
    PAUSED,

    /**
     * Transfer has been completed successfully
     */
    DONE,

    /**
     * Transfer has failed
     */
    FAILED,

    /**
     * Transfer has been cancelled by the user
     */
    CANCELLED,

    /**
     * Transfer is queued but not yet started
     */
    QUEUED,
}