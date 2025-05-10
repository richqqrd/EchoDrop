package com.example.echodrop.model.domainLayer.usecase.file

import com.example.echodrop.model.domainLayer.model.FileEntry
import com.example.echodrop.model.domainLayer.model.PaketId
import com.example.echodrop.model.domainLayer.repository.FileRepository
import javax.inject.Inject

/**
 * Use case for retrieving the list of files associated with a specific `Paket`.
 *
 * @property repo The repository used to access file entry data.
 */
class GetFilesForPaketUseCase @Inject constructor(
    private val repo: FileRepository
) {
    /**
     * Invokes the use case to retrieve the list of files for a specific `Paket`.
     *
     * @param paketId The unique identifier of the `Paket` whose files are to be retrieved.
     * @return A list of `FileEntry` objects associated with the given `Paket`.
     */
    suspend operator fun invoke(paketId: PaketId): List<FileEntry> = repo.getFilesFor(paketId)
}