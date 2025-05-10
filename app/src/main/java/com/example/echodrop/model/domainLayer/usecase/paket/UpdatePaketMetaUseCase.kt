package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import javax.inject.Inject

/**
 * Use case for updating the metadata of a specific `Paket`.
 *
 * @property repo The repository used to manage `Paket` data.
 */
class UpdatePaketMetaUseCase @Inject constructor(
    private val repo: PaketRepository
) {
    /**
     * Invokes the use case to update the metadata of a `Paket`.
     *
     * @param id The unique identifier of the `Paket` to update.
     * @param ttlSeconds The new time-to-live (TTL) value in seconds.
     * @param priority The new priority value for the `Paket`.
     */
    suspend operator fun invoke(id: PaketId, ttlSeconds: Int, priority: Int) =
        repo.updateMeta(id, ttlSeconds, priority)
}