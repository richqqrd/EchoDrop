package com.example.echodrop.model.domain
/**
 * Represents the state of a transfer operation.
 */
enum class TransferState {
    QUEUED,
    ACTIVE,
    DONE,
    FAILED
}