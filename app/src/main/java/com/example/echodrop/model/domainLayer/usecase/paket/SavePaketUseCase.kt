package com.example.echodrop.model.domainLayer.usecase.paket

import com.example.echodrop.model.domainLayer.model.Paket
import com.example.echodrop.model.domainLayer.repository.PaketRepository
import javax.inject.Inject

class SavePaketUseCase @Inject constructor(
    private val paketRepository: PaketRepository
) {
    suspend operator fun invoke(paket: Paket) {
        paketRepository.upsert(paket)
    }
}