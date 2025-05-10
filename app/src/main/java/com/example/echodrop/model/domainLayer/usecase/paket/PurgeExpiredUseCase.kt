package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.repository.PaketRepository
import javax.inject.Inject

/**
 * Use case for purging expired `Paket` entries.
 *
 * @property repo The repository used to manage `Paket` data.
 */
class PurgeExpiredUseCase @Inject constructor(
    private val repo: PaketRepository
) {
    /**
     * Invokes the use case to purge expired `Paket` entries.
     *
     * @param nowUtc The current UTC time in milliseconds.
     * @return The number of `Paket` entries that were purged.
     */
    suspend operator fun invoke(nowUtc: Long): Int = repo.purgeExpire(nowUtc)
}