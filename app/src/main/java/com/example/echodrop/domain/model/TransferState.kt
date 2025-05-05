package com.example.echodrop.domain.model
/**
 * Represents the state of a transfer operation.
 */
enum class TransferState {
    QUEUED,
    ACTIVE,
    DONE,
    FAILED
}