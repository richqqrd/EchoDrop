package com.example.echodrop.model.domainLayer.usecase.peer

import com.example.echodrop.model.domainLayer.model.Peer
import com.example.echodrop.model.domainLayer.repository.PeerRepository
import javax.inject.Inject

class SavePeerUseCase @Inject constructor(
    private val peerRepository: PeerRepository
) {
    suspend operator fun invoke(peer: Peer) {
        peerRepository.upsertPeer(peer)
    }
}