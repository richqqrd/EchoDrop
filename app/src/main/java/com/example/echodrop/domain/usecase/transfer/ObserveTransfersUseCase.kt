package com.example.echodrop.domain.usecase.transfer

import com.example.echodrop.domain.model.TransferLog
import com.example.echodrop.model.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing the list of transfer logs.
 *
 * @property repo The repository used to manage transfer log data.
 */
class ObserveTransfersUseCase @Inject constructor(
    private val repo: TransferRepository
) {
    /**
     * Invokes the use case to observe the list of transfer logs.
     *
     * @return A flow emitting a list of `TransferLog` objects.
     */
    operator fun invoke(): Flow<List<TransferLog>> = repo.observeTransfers()
}