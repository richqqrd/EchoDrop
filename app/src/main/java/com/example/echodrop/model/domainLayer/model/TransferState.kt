package com.example.echodrop.model.domainLayer.model
/**
 * Represents the state of a transfer operation.
 */
enum class TransferState {
    QUEUED,
    ACTIVE,
    DONE,
    FAILED
}