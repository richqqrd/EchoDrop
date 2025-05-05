package com.example.echodrop.domain.usecase.paket

import com.example.echodrop.domain.model.FileEntry
import com.example.echodrop.domain.model.PaketId
import com.example.echodrop.domain.model.PaketMeta
import com.example.echodrop.model.repository.PaketRepository
import javax.inject.Inject

/**
 * Use case for creating a new `Paket`.
 *
 * @property repo The repository used to manage `Paket` data.
 */
class CreatePaketUseCase @Inject constructor(
    private val repo: PaketRepository
) {
    /**
     * Invokes the use case to create a new `Paket`.
     *
     * @param meta The metadata of the `Paket` to be created.
     * @param files The list of files to be associated with the `Paket`.
     * @return The unique identifier of the newly created `Paket`.
     */
    suspend operator fun invoke(meta: PaketMeta, files: List<FileEntry>): PaketId =
        repo.insert(meta, files)

}