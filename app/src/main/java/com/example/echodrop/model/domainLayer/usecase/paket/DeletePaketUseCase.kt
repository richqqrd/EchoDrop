package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import javax.inject.Inject

/**
 * Use case for deleting a specific `Paket`.
 *
 * @property repo The repository used to manage `Paket` data.
 */
class DeletePaketUseCase @Inject constructor(
    private val repo: PaketRepository
) {
    /**
     * Invokes the use case to delete a specific `Paket`.
     *
     * @param id The unique identifier of the `Paket` to delete.
     */
    suspend operator fun invoke(id: PaketId) = repo.delete(id)
}