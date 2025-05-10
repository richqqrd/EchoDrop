package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for observing the inbox of `Paket` objects.
 *
 * @property repo The repository used to access `Paket` data.
 */
class ObserveInboxUseCase @Inject constructor(
    private val repo : PaketRepository
) {
    /**
     * Invokes the use case to observe the inbox.
     *
     * @return A flow emitting a list of `Paket` objects.
     */
    operator fun invoke(): Flow<List<Paket>> = repo.observeInbox()

}