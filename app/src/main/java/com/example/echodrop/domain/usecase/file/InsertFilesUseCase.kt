package com.example.echodrop.domain.usecase.file

import com.example.echodrop.domain.model.FileEntry
import com.example.echodrop.domain.model.PaketId
import com.example.echodrop.model.repository.FileRepository
import javax.inject.Inject

/**
 * Use case for inserting a list of files associated with a specific `Paket`.
 *
 * @property repo The repository used to manage file entry data.
 */
class InsertFilesUseCase @Inject constructor(
    private val repo: FileRepository
) {
    /**
     * Invokes the use case to insert a list of files for a specific `Paket`.
     *
     * @param paketId The unique identifier of the `Paket` to associate the files with.
     * @param files The list of `FileEntry` objects to be inserted.
     */
    suspend operator fun invoke(paketId: PaketId, files: List<FileEntry>) {
        repo.insertAll(paketId, files)
    }

}