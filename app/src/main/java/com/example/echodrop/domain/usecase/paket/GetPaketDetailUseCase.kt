package com.example.echodrop.domain.usecase.paket

import com.example.echodrop.domain.model.Paket
import com.example.echodrop.domain.model.PaketId
import com.example.echodrop.model.repository.PaketRepository
import javax.inject.Inject

/**
 * Use case for retrieving the details of a specific `Paket`.
 *
 * @property repo The repository used to access `Paket` data.
 */
class GetPaketDetailUseCase @Inject constructor(
    private val repo: PaketRepository
) {
    /**
     * Invokes the use case to retrieve the details of a `Paket`.
     *
     * @param id The unique identifier of the `Paket` to retrieve.
     * @return The `Paket` object if found, or `null` if not found.
     */
    suspend operator fun invoke(id: PaketId): Paket? = repo.getPaket(id)
}
