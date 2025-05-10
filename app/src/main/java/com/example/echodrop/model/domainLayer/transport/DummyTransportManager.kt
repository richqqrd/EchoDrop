package com.example.echodrop.model.domainLayer.transport

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransportManagerImpl @Inject constructor():
    TransportManager {
    override fun startBeaconing() {
        TODO("Not yet implemented")
    }

    override fun stopBeaconing() {
        TODO("Not yet implemented")
    }

    override suspend fun sendManifest(paketId: String, manifestJson: String) {
        TODO("Not yet implemented")
    }

    override suspend fun sendChunk(chunkId: String, data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun observeIncoming(): Flow<IncomingFrame> {
        TODO("Not yet implemented")
    }


}